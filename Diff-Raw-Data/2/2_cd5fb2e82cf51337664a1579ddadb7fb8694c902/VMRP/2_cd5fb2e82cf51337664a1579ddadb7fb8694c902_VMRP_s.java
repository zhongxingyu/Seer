 /**
  * Greedy_Host_Evacuation_in_Cloud - Technion, Israel Institute of Technology
  * 
  * Author: Assaf Israel, 2013
  * Created: Jun 26, 2013
  */
 package il.ac.technion.glpk;
 
 import static org.gnu.glpk.GLPK.*;
 import static org.gnu.glpk.GLPKConstants.GLP_ON;
 import il.ac.technion.datacenter.physical.Host;
 import il.ac.technion.datacenter.physical.PhysicalAffinity;
 import il.ac.technion.datacenter.vm.VM;
 import il.ac.technion.misc.CollectionUtils;
 import il.ac.technion.misc.LazyIterator;
 import il.ac.technion.misc.Predicate;
 import il.ac.technion.misc.Tuple;
 import il.ac.technion.rigid.RecoveryPlan;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.gnu.glpk.SWIGTYPE_p_double;
 import org.gnu.glpk.SWIGTYPE_p_int;
 import org.gnu.glpk.glp_iocp;
 import org.gnu.glpk.glp_prob;
 
 public class VMRP  {
 
 	private static Logger logger = Logger.getLogger(VMRP.class);
 	
 	private class VMRPFormulationKit {
 		public final glp_prob lp;
 		public final int[] y;
 		public final int[][] m;
 		public final Map<Host,List<Integer>> hostIdxMapping = new HashMap<>();
 		public final Map<VM,Integer> vmIdxMapping = new HashMap<>();
 		
 		public VMRPFormulationKit(glp_prob lp, int[] y, int[][] m) {
 			this.lp = lp;
 			this.y = y;
 			this.m = m;
 		}
 	}
 
 	public RecoveryPlan solve(List<PhysicalAffinity> pal) throws Exception { 
 		
 		VMRPFormulationKit vfk = createProblemFormulation(pal); 
 		final glp_prob lp =  vfk.lp;
 		glp_print_mip(lp, "vmrp.glp");
 		glp_iocp iocp = new glp_iocp();
 		glp_init_iocp(iocp);
//		iocp.setPresolve(GLP_ON);
 		logger.info("Solving VMRP ...");
 		try {
 			if (glp_intopt(lp,iocp) != 0) {
 				logger.error("The problem could not be solved");
 				throw new Exception("The problem could not be solved");
 			}	
 			logger.info("Solution found");
 			GLPKUtils.writeMipSolution(lp);
 			return GLPKUtils.extractRecoveryPlan(lp, vfk.y, vfk.m, vfk.hostIdxMapping, vfk.vmIdxMapping);
 		} finally {
 			logger.info("Clean up");
 			glp_delete_prob(lp);
 		}
 	}
 
 	private VMRPFormulationKit createProblemFormulation(List<PhysicalAffinity> pal) {
 		logger.info("Creating VMRP Formulation...");
 		glp_prob lp = glp_create_prob();
 		glp_set_prob_name(lp,"VM Recovery Problem");
 		return topDownFormulation(lp,pal);
 	}
 
 	private VMRPFormulationKit topDownFormulation(glp_prob lp, List<PhysicalAffinity> pal) {
 		logger.info("Creting objective function");
 		glp_set_obj_dir(lp,GLP_MIN);
 		
 		logger.info("Defining Y-vector (Host Activation)");
 		
 		Tuple<List<Host>,List<Host>> H_bar = CollectionUtils.partition(PhysicalAffinity.extractHosts(pal), new Predicate<Host>(){
 			@Override
 			public boolean accept(Host h) {
 				return h.active();
 			}
 		});
 		
 		List<VM> V_bar = PhysicalAffinity.extractVMs(pal);
 		
 		int H_bar_len = hBarLength(H_bar);
 		int V_bar_len = vBarLength(V_bar);
 		
 		int[] y = new int[H_bar_len];
 		int[][] m = new int[H_bar_len][V_bar_len];
 		
 		VMRPFormulationKit vfk = new VMRPFormulationKit(lp, y, m);
 		Map<Integer,Host> inverseHostIdx = new HashMap<>();
 		
 		// Sum a[i]y[i]
 		LazyIterator<Host> hIter = new LazyIterator<>(H_bar._1,H_bar._2);
 		for (int i = 1; i < H_bar_len; i++) {
 			i = setActivationVector(lp,y,vfk.hostIdxMapping,inverseHostIdx,i, hIter.next());
 		}
 
 		// Sum c[i][j]m[i][j]
 		for (int i = 1; i < H_bar_len; i++) {
 			Host h = inverseHostIdx.get(i);
 			Iterator<VM> vmIter = V_bar.iterator();
 			for (int j = 1; j < V_bar_len; j++) {
 				VM vm = vmIter.next();
 				setRecoveryMatrix(lp,y,m,vfk.vmIdxMapping,i,j,h,vm,coResident(pal,h,vm));
 			}
 		}
 		
 		logger.info("Defining Constraints..");
 		createVmAssignmentCons(lp,m,H_bar_len,V_bar_len);		
 		createAssignVmToMarkedHostsCons(lp, m, y, H_bar_len,V_bar_len);
 		createCapacityConst(lp, y, m, H_bar_len,V_bar_len,pal,vfk.vmIdxMapping,inverseHostIdx);
 		createSingleActivationConst(lp,y,vfk.hostIdxMapping,H_bar._2);
 		
 		return vfk;
 	}
 
 	private int vBarLength(List<VM> V_bar) {
 		return V_bar.size() + 1;
 	}
 
 	private int hBarLength(Tuple<List<Host>, List<Host>> H_bar) {
 		return H_bar._1.size() + H_bar._2.size()*2 + 1;
 	}
 	
 	private boolean coResident(final List<PhysicalAffinity> pal, final Host h, final VM vm) {
 		return CollectionUtils.find(pal, new Predicate<PhysicalAffinity>(){
 			@Override
 			public boolean accept(PhysicalAffinity pa) {
 				return pa.hosts().contains(h) && pa.vms().contains(vm);
 			}});
 	}
 
 	private int setActivationVector(glp_prob lp, int[] y, Map<Host,List<Integer>> hostIdxMapping, Map<Integer, Host> inverseHostIdx, int i, Host h) {
 		setActivationVariable(lp,y,hostIdxMapping,inverseHostIdx,i, h);
 		if (h.inactive()) {
 			h.activate();
 			setActivationVariable(lp,y,hostIdxMapping,inverseHostIdx,i+1,h);
 			h.deactivate();
 			return i+1;
 		}
 		return i;
 	}
 
 	private void setActivationVariable(glp_prob lp, int[] y, Map<Host, List<Integer>> hostIdxMapping, Map<Integer, Host> inverseHostIdx, int i, Host h) {
 		y[i] = glp_add_cols(lp,1);
 		String name = "y"+(h.inactive() ? "^off" : "")+"["+i+"]";
 		glp_set_col_name(lp,y[i],name);
 		glp_set_col_kind(lp,y[i],GLP_BV);
 		glp_set_obj_coef(lp,y[i],h.cost());
 		
 		addToMapList(h,new Integer(i),hostIdxMapping);
 		inverseHostIdx.put(i, h);
 	}
 
 	private void setRecoveryMatrix(glp_prob lp, int[] y, int[][] m, Map<VM, Integer> vmIdxMapping, 
 			int i, int j, Host host, VM vm, boolean colocated) {
 		if (glp_get_col_name(lp,y[i]).contains("off")) {
 			host.deactivate();
 		} else {
 			host.activate();
 		}
 		setRecoveryVariable(lp, m, i, j, host, vm, colocated);
 		host.resetActivation();
 		vmIdxMapping.put(vm, j);
 	}
 
 	private void setRecoveryVariable(glp_prob lp, int[][] m, int i, int j,
 			Host h, VM vm, boolean colocated) {
 		m[i][j] = glp_add_cols(lp,1);
 		String name = "m["+i+"]["+j+"]";
 		glp_set_col_name(lp,m[i][j],name);
 		glp_set_col_kind(lp,m[i][j],GLP_BV);
 		glp_set_obj_coef(lp,m[i][j],colocated ? Double.MAX_VALUE : vm.cost(h));
 	}
 
 	/**
 	 * Every VM will be assigned to some host
 	 * Sum_(i in H_bar) m[i][j] = 1, for every j in V_bar
 	 */
 	private void createVmAssignmentCons(glp_prob lp,int[][] m,int H_bar_len,int V_bar_len) {
 		logger.info("Defining VM Assignment Constraints");
 		SWIGTYPE_p_int ind = new_intArray(H_bar_len);
 		SWIGTYPE_p_double val = new_doubleArray(H_bar_len);
 		for (int j = 1; j < V_bar_len; j++) {
 			for (int i = 1; i < H_bar_len; i++) {
 				intArray_setitem(ind,i,m[i][j]);
 				doubleArray_setitem(val,i,1); // +1*m[i][j]
 			}
 			logger.info("Defining C1-VM["+j+"]");
 
 			int row = glp_add_rows(lp,1);
 			glp_set_row_name(lp,row,"C1-VM["+j+"]");
 			glp_set_row_bnds(lp,row,GLP_FX,1,1); // aX = 1
 			glp_set_mat_row(lp,row,H_bar_len-1,ind,val);
 		}
 		delete_doubleArray(val);
 		delete_intArray(ind);
 	}
 	
 	/**
 	 * Every VM will be assigned to marked hosts only
 	 * m[i][j] - y[i] <= 0, for every i in H_bar and j in V_bar
 	 */
 	private void createAssignVmToMarkedHostsCons(glp_prob lp,int[][] m, int[] y,int H_bar_len, int V_bar_len) {
 		logger.info("Defining Assign VM to Marked Hosts Constraints");
 		SWIGTYPE_p_int ind = new_intArray(3);
 		SWIGTYPE_p_double val = new_doubleArray(3);
 		for (int j = 1; j < V_bar_len; j++) {
 			for (int i = 1; i < H_bar_len; i++) {
 				intArray_setitem(ind,1,m[i][j]);
 				doubleArray_setitem(val,1,1); // 1*m[i][j]
 				intArray_setitem(ind,2,y[i]);
 				doubleArray_setitem(val,2,-1); // -1*y[i]
 		
 				logger.info("Defining C2-M["+i+"]["+j+"]-Y["+j+"]");
 			
 				int row = glp_add_rows(lp,1);
 				glp_set_row_name(lp,row,"C2-M["+i+"]["+j+"]-Y["+j+"]");
 				glp_set_row_bnds(lp,row,GLP_UP,0,0); // aX <= 0
 				glp_set_mat_row(lp,row,2,ind,val);
 			}
 		}
 		delete_doubleArray(val);
 		delete_intArray(ind);
 	}
 	
 	/**
 	 * Capacity constraints originating from the same Sum_(j in V^r) m[i][j]*p[i][j] - y[i] <= 0 for every i,r in H_bar and j in V^r
 	 */
 	private void createCapacityConst(glp_prob lp, int[] y, int[][] m,int h_bar_len, int v_bar_len, List<PhysicalAffinity> pal,
 			Map<VM, Integer> vmIdxMapping, Map<Integer, Host> inverseHostIdx) {
 		logger.info("Defining Capacity Constraints");
 		for (PhysicalAffinity pa : pal) {
 			List<VM> paVMs = pa.vms();
 			SWIGTYPE_p_int ind = new_intArray(paVMs.size() + 2);
 			SWIGTYPE_p_double val = new_doubleArray(paVMs.size() + 2);
 			for (int i = 1; i < h_bar_len; i++) {
 				for (VM vm : paVMs) {
 					int idxInArr = paVMs.indexOf(vm) + 1;
 					intArray_setitem(ind,idxInArr,m[i][vmIdxMapping.get(vm)]);
 					doubleArray_setitem(val,idxInArr,relativeSize(vm,inverseHostIdx.get(i))); // m[i][j]*p[i][j]
 				}
 				intArray_setitem(ind,paVMs.size() + 1,y[i]);
 				doubleArray_setitem(val,paVMs.size() + 1,-1); // -1*y[i]
 		
 				logger.info("Defining C3-H["+i+"]-PA["+pal.indexOf(pa)+"]");
 				
 				int row = glp_add_rows(lp,1);
 				glp_set_row_name(lp,row,"C3-H["+i+"]-PA["+pal.indexOf(pa)+"]");
 				glp_set_row_bnds(lp,row,GLP_UP,0,0); // aX <= 0
 				glp_set_mat_row(lp,row,paVMs.size() + 1,ind,val);
 			}
 			delete_doubleArray(val);
 			delete_intArray(ind);
 		}
 	}
 	
 	/**
 	 * Single activation constraints for the H^off hosts. y[i]+y'[i] <= 1 for every i in H^off.
 	 */
 	private void createSingleActivationConst(glp_prob lp, int[] y, Map<Host, List<Integer>> hostIdxMapping, List<Host> backupHosts) {
 		logger.info("Defining Single Activation Constraints");
 		SWIGTYPE_p_int ind = new_intArray(3);
 		SWIGTYPE_p_double val = new_doubleArray(3);
 		
 		for (Host host : backupHosts) {
 			List<Integer> hostIndexes = hostIdxMapping.get(host);
 			
 			// Sanity check
 			if (hostIndexes.size() != 2) 
 				throw new RuntimeException("Wrong number of host indexes ["+hostIndexes.size()+"]");
 			
 			StringBuilder name = new StringBuilder();
 			for (Integer i : hostIndexes) {
 				intArray_setitem(ind,hostIndexes.indexOf(i)+1,y[i]);
 				doubleArray_setitem(val,hostIndexes.indexOf(i)+1,1); // 1*y[i] or 1*y'[i]
 				name.append("-H["+i+"]");
 			}
 		
 			logger.info("Defining C4"+name);
 			
 			int row = glp_add_rows(lp,1);
 			glp_set_row_name(lp,row,"C4"+name);
 			glp_set_row_bnds(lp,row,GLP_UP,0,1); // aX <= 1
 			glp_set_mat_row(lp,row,2,ind,val);
 		}
 		delete_doubleArray(val);
 		delete_intArray(ind);
 	}
 
 	private double relativeSize(VM vm, Host host) {
 		if (vm.size() > host.freeCapacity()) 
 			return Double.MAX_VALUE;
 		return (double)vm.size() / host.freeCapacity();
 	}
 
 	private static <T,S> void addToMapList(T t, S s, Map<T,List<S>> map) {
 		if (map.containsKey(t)) 
 			map.get(t).add(s); 
 		else {
 			List<S> sl = new ArrayList<S>();
 			sl.add(s);
 			map.put(t, sl);
 		}
 	}
 }

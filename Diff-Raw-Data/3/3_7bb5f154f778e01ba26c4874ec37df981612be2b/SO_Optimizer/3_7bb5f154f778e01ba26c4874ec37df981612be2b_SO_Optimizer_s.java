 package dta_solver;
 
 import io.InputOutput;
 
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import generalLWRNetwork.Cell;
 import generalLWRNetwork.Destination;
 import generalLWRNetwork.Junction;
 import generalLWRNetwork.Origin;
 import generalNetwork.state.CellInfo;
 import generalNetwork.state.JunctionInfo;
 import generalNetwork.state.Profile;
 import generalNetwork.state.State;
 import generalNetwork.state.externalSplitRatios.IntertemporalOriginsSplitRatios;
 import generalNetwork.state.internalSplitRatios.IntertemporalJunctionSplitRatios;
 import generalNetwork.state.internalSplitRatios.IntertemporalSplitRatios;
 import generalNetwork.state.internalSplitRatios.JunctionSplitRatios;
 
 import cern.colt.matrix.tdouble.DoubleMatrix1D;
 import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
 import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
 import cern.colt.matrix.tdouble.DoubleFactory1D;
 import dataStructures.Numerical;
 import dataStructures.Triplet;
 import dta_solver.adjointMethod.Adjoint;
 
 public class SO_Optimizer extends Adjoint<State> {
 
   protected Simulator simulator;
 
   private double epsilon = 0.001;
 
   /* Share of the compliant commodities */
   private double alpha;
   /* Total number of time steps */
   protected int T;
   /* Number of compliant commodities */
   protected int C;
   protected Cell[] cells;
   protected Junction[] junctions;
   protected Origin[] sources;
   protected Destination[] destinations;
   /* Number of origins */
   protected int O;
   /* Number of destinations */
   private int S;
 
   /* Control Vector U */
   /* Total size of a block for one time step of the control */
   protected int temporal_control_block_size;
 
   /* State Vector X */
   /* Size of a block describing all the densities for a given time step */
   protected int size_density_block;
   /* Size of a block describing all the supply/demand at one time step */
   protected int size_demand_suply_block;
   /* Size of the block describing all the Aggregate SR at one time sate */
   protected int size_aggregate_split_ratios;
   /* Size of a block describing out-flows */
   protected int size_f_out_block;
   /* Total size of the description of a profile for a given time step */
   protected int x_block_size;
   protected int demand_supply_position;
   protected int aggregate_split_ratios_position;
   protected int f_out_position;
   protected int f_in_position;
 
   /* Constraints Vector H */
   /* Size of a block describing the Mass Conversation constraints */
   // size_density_block;
   /* Size of a block describing the Flow Propagation constraints */
   private int flow_propagation_size;
 
   /* Size of the block describing the Aggregate SR constraints */
   // size_aggregate_split_ratios;
   /* Size of the block describing the out-flows constraints : */
   // mass_conservation_size
   /* Size of the block describing the in-flows constraints : */
   // mass_conservation_size
   /* Total size of a block of constraints for a given time step */
   // x_block_size
 
   public SO_Optimizer(int maxIter, Simulator simu) {
     super(maxIter);
     simulator = simu;
 
     alpha = simu.getAlpha();
     T = simulator.time_discretization.getNb_steps();
     C = simulator.lwr_network.getNb_compliantCommodities();
     cells = simulator.lwr_network.getCells();
 
     sources = simulator.lwr_network.getSources();
     O = sources.length;
     destinations = simulator.lwr_network.getSinks();
     S = destinations.length;
 
     junctions = simulator.lwr_network.getJunctions();
 
     /* For every time steps there are C compliant flows, and O non compliant */
     temporal_control_block_size = C;
 
     /* State Vector X */
     /* Size of a block describing all the densities for a given time step */
     size_density_block = cells.length * (C + 1);
     /* Size of a block describing all the supply/demand at one time step */
     size_demand_suply_block = 2 * cells.length;
     /* Size of the block describing all the Aggregate SR at one time sate */
     Junction junction;
     int tmp_nb_aggregate_split_ratios = 0;
     for (int j = 0; j < junctions.length; j++) {
       junction = junctions[j];
       tmp_nb_aggregate_split_ratios +=
           junction.getPrev().length * junction.getNext().length;
     }
     size_aggregate_split_ratios = tmp_nb_aggregate_split_ratios;
     /* Size of a block describing out-flows or in-flows */
     size_f_out_block = size_density_block;
     /* Total size of the description of a profile for a given time step */
     x_block_size = (3 * (C + 1) + 2) * cells.length
         + size_aggregate_split_ratios;
 
     demand_supply_position = size_density_block;
     aggregate_split_ratios_position =
         demand_supply_position + size_demand_suply_block;
     f_out_position =
         aggregate_split_ratios_position + size_aggregate_split_ratios;
     f_in_position = f_out_position + size_f_out_block;
 
     /* Constraints Vector H */
     /* Size of a block describing the Mass Conversation constraints */
     // size_density_block;
     /* Size of a block describing the Flow Propagation constraints */
     flow_propagation_size = size_demand_suply_block;
     /* Size of the block describing the Aggregate SR constraints */
     // size_aggregate_split_ratios;
     /* Size of the block describing the out-flows constraints : */
     // mass_conservation_size
     /* Size of the block describing the in-flows constraints : */
     // mass_conservation_size
     /* Total size of a block of constraints for a given time step */
     int H_block_size = 3 * size_density_block + flow_propagation_size
         + size_aggregate_split_ratios;
 
     assert (H_block_size == x_block_size);
     /* Initialization of the split ratios for the Optimizer */
     simulator.initializeSplitRatiosForOptimizer();
   }
 
   public void printSizes() {
     System.out.println("*******************************************");
     System.out.println("Total size of X and H: " + T * x_block_size);
     System.out.println("Details: \n" +
         "- time steps: " + T + "\n" +
         "- density_block: " + size_density_block +
         " : (1 NC + " + C + " compliant commodities )* " + cells.length
         + " cells\n" +
         "- demand_supply: " + size_demand_suply_block +
         " : 2 (for demand, supply) * " + cells.length + " cells)\n" +
         "- aggregate SR: " + size_aggregate_split_ratios +
         " (is the sum of the in.size() * out.size() at all junctions)\n" +
         "- f_out: " + size_density_block +
         " : (same as the density block)\n" +
         "- f_ in: " + size_density_block +
         " : (same as the density block)");
     System.out.println("*******************************************");
   }
 
   /**
    * @brief Return the 1x(C*T) matrix representing the control where
    *        C is the number of compliant commodities
    * @details There are T blocks of size C. The i-th block contains the
    *          controls at time step i.
    */
   public double[] getControl() {
 
     assert alpha != 0 : "The share of the compliant commodities is zero. No optimization possible";
     IntertemporalOriginsSplitRatios splits = simulator.splits;
 
     /* For every time steps there are C compliant flows */
     double[] control = new double[T * temporal_control_block_size];
 
     int index_in_control = 0;
     int commodity;
     Double split_ratio;
     for (int orig = 0; orig < O; orig++) {
       Iterator<Integer> it = sources[orig]
           .getCompliant_commodities()
           .iterator();
       while (it.hasNext()) {
         commodity = it.next();
         for (int k = 0; k < T; k++) {
           /*
            * Mapping between splits.get(sources[orig], k).get(commodity) and
            * U[k*(C + sources.length) + index_in_control]
            */
           split_ratio = splits.get(sources[orig], k).get(commodity);
           if (split_ratio != null)
             // The sum of the split ratios at the control should be equal to 1
             // for a given origin
             control[k * temporal_control_block_size + index_in_control] = split_ratio
                 / alpha;
           assert split_ratio / alpha >= 0 : "We are exporting a negative control";
 
         }
         index_in_control++;
       }
     }
 
     return control;
   }
 
   /**
    * @brief Return the 1x(C+O)*T= matrix representing the compliant and
    *        non-compliant commodities where C is the number of compliant
    *        commodities and 0 the number of origins
    * @details There are T blocks of size (C+O). The i-th block contains the
    *          controls at time step i.
    */
   private double[] getFullControl() {
 
     IntertemporalOriginsSplitRatios splits = simulator.splits;
 
     /* For every time steps there are C compliant flows, and O non compliant */
     double[] control = new double[T * (C + 1)];
 
     int index_in_control = 0;
     int commodity;
     Double split_ratio;
     for (int orig = 0; orig < O; orig++) {
       for (int k = 0; k < T; k++) {
         split_ratio = splits.get(sources[orig], k).get(0);
         if (split_ratio != null) {
           control[k * temporal_control_block_size + index_in_control] = split_ratio;
         }
       }
       index_in_control++;
 
       Iterator<Integer> it = sources[orig]
           .getCompliant_commodities()
           .iterator();
       while (it.hasNext()) {
         commodity = it.next();
         for (int k = 0; k < T; k++) {
           split_ratio = splits.get(sources[orig], k).get(commodity);
           if (split_ratio != null) {
            control[k * temporal_control_block_size + index_in_control] = split_ratio
                * alpha;
           }
         }
         index_in_control++;
       }
     }
     return control;
   }
 
   public void printFullControl() {
     InputOutput.printControl(getFullControl(), C + 1);
   }
 
   private void parseStateVector(Profile p) {
 
     int block_id, sub_block_id;
     int commodity;
     int index_in_state = 0;
     double value;
     CellInfo cell_info;
     for (int k = 0; k < T; k++) {
       /* Id of the first data of time step k */
       block_id = k * x_block_size;
 
       for (int cell_id = 0; cell_id < cells.length; cell_id++) {
 
         cell_info = p.getCell(cells[cell_id]);
         /* Id of the first index containing data from cells[cell_id] */
         sub_block_id = block_id + cell_id * C;
 
         // Operations on densities
         Iterator<Entry<Integer, Double>> it =
             cell_info.partial_densities.entrySet().iterator();
         Entry<Integer, Double> entry;
         while (it.hasNext()) {
           entry = it.next();
           commodity = entry.getKey();
           // density (cell_id, commodity)(k)
           index_in_state = sub_block_id + commodity;
           value = entry.getValue();
         }
 
         // Operations on demand and supply
         index_in_state = sub_block_id + size_density_block;
         value = cell_info.demand;
         index_in_state++;
         value = cell_info.supply;
 
         // Operations on aggregate split ratios
         index_in_state += size_demand_suply_block;
         JunctionInfo junction_info;
         Junction junction;
         for (int j = 0; j < junctions.length; j++) {
           junction = junctions[j];
           junction_info = p.getJunction(j);
           for (int in = 0; in < junction.getPrev().length; in++) {
             for (int out = 0; out < junction.getNext().length; out++) {
               // Mapping between junctions_info.get(new PairCell(in, out));
               index_in_state++;
             }
           }
         }
 
         // Operation on out-flows
         sub_block_id += size_aggregate_split_ratios;
         it = cell_info.out_flows.entrySet().iterator();
         while (it.hasNext()) {
           entry = it.next();
           commodity = entry.getKey();
           // flow_out (cell_id, commodity)(k)
           index_in_state = sub_block_id + commodity;
           value = entry.getValue();
         }
 
         // Operations on in-flows
         index_in_state += size_f_out_block;
         it = cell_info.in_flows.entrySet().iterator();
         while (it.hasNext()) {
           entry = it.next();
           commodity = entry.getKey();
           // flow_in (cell_id, commodity)(k)
           index_in_state = sub_block_id + commodity;
           value = entry.getValue();
         }
       }
     }
   }
 
   public void informationIndexInX(int i) {
     int time_step = i / x_block_size;
     System.out.print("[k=" + time_step + "]");
     int remaining = i % x_block_size;
     if (remaining < demand_supply_position) {
       int cell_id = remaining / (C + 1);
       int c = (remaining % (C + 1));
       System.out.println("Partial density of commodity " + c + " in cell "
           + (cell_id));
     } else if (remaining < aggregate_split_ratios_position) {
       int cell_id = (remaining - demand_supply_position) / 2;
       int is_demand = ((remaining - demand_supply_position - cell_id * 2) % 2);
       if (is_demand == 0)
         System.out.println("Demand in cell " + (cell_id));
       else
         System.out.println("Supply in cell " + (cell_id));
     } else if (remaining < f_out_position) {
       int cell_id = (remaining - aggregate_split_ratios_position) / (C + 1);
       int c = (remaining % (C + 1));
       System.out
           .println("Aggregate split ratio");
     } else if (remaining < f_in_position) {
       int cell_id = (remaining - f_out_position) / (C + 1);
       int c = (remaining % (C + 1));
       System.out
           .println("Flow-out of commodity " + c + " in cell " + (cell_id));
     } else {
       int cell_id = (remaining - f_in_position) / (C + 1);
       int c = (remaining % (C + 1));
       System.out
           .println("Flow-in of commodity " + c + " in cell " + (cell_id));
     }
   }
 
   /**
    * @brief Computes the dH/dU matrix.
    * @details
    */
   @Override
   public SparseCCDoubleMatrix2D dhdu(State state, double[] control) {
 
     SparseCCDoubleMatrix2D result = new SparseCCDoubleMatrix2D(
         x_block_size * T,
         temporal_control_block_size * T);
 
     int i, j, index_in_control = 0;
     int commodity;
     double[] origin_demands;
     for (int orig = 0; orig < O; orig++) {
       origin_demands = simulator.origin_demands.get(sources[orig]);
 
       Iterator<Integer> it = sources[orig]
           .getCompliant_commodities()
           .iterator();
       while (it.hasNext()) {
         commodity = it.next();
         for (int k = 0; k < T; k++) {
           i = k * x_block_size + sources[orig].getUniqueId() * (C + 1)
               + commodity;
           j = k * temporal_control_block_size + index_in_control;
 
           assert (cells[sources[orig].getUniqueId()].getLength() == 1) : "For now buffers must have a length of 1.0";
           /*
            * For now, origin_demands[k] is already a number of vehicle. So we do
            * not multiply by the time step
            */
           double value = origin_demands[k] * alpha;
           result.setQuick(i, j, value);
         }
         index_in_control++;
       }
     }
 
     return result;
   }
 
   @Override
   public SparseCCDoubleMatrix2D dhdx(State state, double[] control) {
 
     IntertemporalSplitRatios internal_SR =
         simulator.lwr_network.getInternal_split_ratios();
 
     SparseCCDoubleMatrix2D result = new SparseCCDoubleMatrix2D(
         x_block_size * T,
         x_block_size * T);
 
     double delta_t = simulator.time_discretization.getDelta_t();
     /* The diagonal terms are done at the end */
 
     /*********************************************************
      * Derivative terms for the Mass Conservation constraints
      *********************************************************/
     // System.out.print("Mass conservation : ");
     // long startTime = System.currentTimeMillis();
     // Position of a block of constraints in H indexed by k
     int block_upper_position;
     // Position of a block in the Mass Conservation block indexed by the cell_id
     int sub_block_position;
     double delta_t_over_l;
     int i, j;
     for (int k = 1; k < T; k++) {
       block_upper_position = k * x_block_size;
       for (int cell_id = 0; cell_id < cells.length; cell_id++) {
         sub_block_position = (C + 1) * cell_id;
         for (int c = 0; c < C + 1; c++) {
 
           // Line of interest in the H matrix
           i = block_upper_position + sub_block_position + c;
           // Column of interest in the H matrix
           j = x_block_size * (k - 1) + sub_block_position + c;
 
           /*
            * We put 1 for the derivative terms of the mass conversation
            * equations (i,c,k) with respect to density(i,c,k-1) for k : [1,T-1]
            */
           result.setQuick(i, j, 1.0);
 
           /*
            * Derivative terms with respect to flow-out(i,c,k-1) and
            * flow-in(i,c,k-1)
            */
           delta_t_over_l = delta_t / cells[cell_id].getLength();
 
           assert Numerical.validNumber(delta_t_over_l);
 
           // d \density(i, k) / d f_out(i,k-1) = - delta_t / l
           result.setQuick(i, j + f_out_position, -delta_t_over_l);
 
           // d \density(i, k) / d f_in(i,k-1) = delta_t / l
           result.setQuick(i, j + f_in_position, delta_t_over_l);
         }
       }
       // For the buffers and sinks we have put a derivative term that should
       // have been zero
       for (int o = 0; o < O; o++) {
         for (int c = 0; c < C + 1; c++) {
           i = block_upper_position + (C + 1) * sources[o].getUniqueId() + c;
           j = x_block_size * (k - 1) + (C + 1) * sources[o].getUniqueId() + c;
           // flow-in
           result.setQuick(i, j + f_in_position, 0.0);
         }
       }
       for (int s = 0; s < S; s++) {
         for (int c = 0; c < C + 1; c++) {
           i = block_upper_position + (C + 1) * destinations[s].getUniqueId()
               + c;
           j = x_block_size * (k - 1) + (C + 1) * destinations[s].getUniqueId()
               + c;
           // flow-out
           result.setQuick(i, j + f_out_position, 0.0);
         }
       }
     }
     // long endTime = System.currentTimeMillis();
     // System.out.println(endTime - startTime);
     /*********************************************************
      * Derivative terms for the Flow propagation (demand/supply)
      *********************************************************/
     // System.out.print("Flow propagation: ");
     // startTime= System.currentTimeMillis();
 
     for (int k = 0; k < T; k++) {
       // Position of the first constraint in H dealing with supply/demand at
       // time step k
       block_upper_position = k * x_block_size + demand_supply_position;
       for (int cell_id = 0; cell_id < cells.length; cell_id++) {
         sub_block_position = cell_id * 2;
         i = block_upper_position + sub_block_position;
         Cell in_cell = cells[cell_id];
         double total_density = state.profiles[k].getCell(cell_id).total_density;
 
         /*
          * In the case of zero density, we should not say that the demand is
          * dependent of the partial densities. See the adjoint equations.
          * In that case, the derivative of the supply is also zero.
          */
         if (total_density == 0)
           continue;
         for (int c = 0; c < C + 1; c++) {
 
           // Demand first
           result.setQuick(i,
               x_block_size * k + cell_id * (C + 1) + c,
               cells[cell_id].getDerivativeDemand(total_density, delta_t));
 
           // Then supply
           /* There is no derivative term with respect to the supply for buffers */
           if (in_cell.isBuffer())
             result.setQuick(i + 1, x_block_size * k + cell_id * (C + 1) + c,
                 cells[cell_id].getDerivativeSupply(total_density));
         }
       }
     }
     // endTime= System.currentTimeMillis();
     // System.out.println(endTime - startTime);
     /*********************************************************
      * Derivative terms for the Aggregate Split Ratios
      *********************************************************/
     // System.out.print("Aggregate SR: ");
     // startTime= System.currentTimeMillis();
     /*
      * This part is not efficient because we have to do
      * Nb_Aggregate_SR * T * (C+1) computation of derivative terms
      */
     /* Used to know the position of the current SR under study */
     int aggregate_SR_index = 0;
     /* Used to store beta(i,j,c)(k) */
     Double i_j_c_SR;
     /* Store the split ratios at a junction. Is null when Nx1 junction */
     JunctionSplitRatios junction_SR;
     CellInfo in_cell_info;
     int prev_length, next_length;
 
     for (int j_id = 0; j_id < junctions.length; j_id++) {
       Junction junction = junctions[j_id];
       IntertemporalJunctionSplitRatios intert_junction_SR =
           internal_SR.get(j_id);
 
       prev_length = junction.getPrev().length;
       for (int in = 0; in < prev_length; in++) {
         Cell in_cell = junction.getPrev()[in];
         next_length = junction.getNext().length;
         for (int out = 0; out < next_length; out++) {
           Cell out_cell = junction.getNext()[out];
           for (int k = 0; k < T; k++) {
             /* Here we study beta(in, out) (k) */
             block_upper_position = k * x_block_size
                 + aggregate_split_ratios_position;
 
             /*
              * There are no inter-temporal split ratios for Nx1 junctions.
              * More precisely for all i \in Incoming link and j the unique
              * outgoing link
              * we have all the split ratio from i to j that is 1 and constant
              */
             if (intert_junction_SR == null)
               junction_SR = null;
             else {
               junction_SR = intert_junction_SR.get(k);
               assert junction_SR != null;
             }
             in_cell_info = state.profiles[k].getCell(in_cell);
 
             i = block_upper_position + aggregate_SR_index;
             j = k * x_block_size + (C + 1) * in_cell.getUniqueId();
 
             for (int c = 0; c < C + 1; c++) {
               /*
                * If there are no intertemporal_split ratios this means we are at
                * a Nx1 junction and we always have beta = 1. However for the 1x1
                * junction, the aggregate spit ratio has absolutely no influence
                * on the flows and we can do without computing it
                * TODO: check this
                * TODO: Optimization possible
                */
               if (junction_SR == null) {
                 assert next_length == 1;
                 // if (prev_length == 1 && next_length == 1)
                 // continue;
                 i_j_c_SR = 1.0;
               } else
                 i_j_c_SR = junction_SR.get(in_cell.getUniqueId(),
                     out_cell.getUniqueId(), c);
 
               /*
                * If the split ratio for this commodity is zero, then the
                * aggregate split ratio is independent of this split ratio
                */
               // if (i_j_c_SR == null || i_j_c_SR == 0)
               // continue;
               if (i_j_c_SR == null) {
                 continue;
                 // i_j_c_SR = 0.0;
               }
               double total_density = in_cell_info.total_density;
 
               if (total_density != 0) {
                 Double partial_density = in_cell_info.partial_densities.get(c);
                 if (partial_density == null)
                   partial_density = 0.0;
 
                 double derivative_term = i_j_c_SR
                     * (total_density - partial_density)
                     / (total_density * total_density);
                 assert Numerical.validNumber(derivative_term);
 
                 if (derivative_term != 0)
                   result.setQuick(i, j + c, derivative_term);
               }
             }
           }
           aggregate_SR_index++;
         }
       }
     }
     // endTime= System.currentTimeMillis();
     // System.out.println(endTime - startTime);
 
     /*********************************************************
      * Derivative terms for the out-flows
      *********************************************************/
     // System.out.print("Out-flows: ");
     // startTime= System.currentTimeMillis();
 
     /*
      * This is used to know the position of the first aggregate split ratio
      * associated with the current junction. It is incremented by nb_prev *
      * nb_next after every loop execution
      */
     int aggregate_index = 0;
     for (int j_id = 0; j_id < junctions.length; j_id++) {
       Junction junction = junctions[j_id];
       int nb_prev = junction.getPrev().length;
       int nb_next = junction.getNext().length;
 
       // Derivative terms for 1x1 junctions
       if (nb_prev == 1 && nb_next == 1) {
         double demand, supply, f_out;
         CellInfo cell_info;
         int prev_id = junction.getPrev()[0].getUniqueId();
         int next_id = junction.getNext()[0].getUniqueId();
         for (int k = 0; k < T; k++) {
           cell_info = state.profiles[k].getCell(prev_id);
           double total_density = cell_info.total_density;
 
           demand = cell_info.demand;
           supply = state.profiles[k].getCell(next_id).supply;
 
           f_out = Math.min(demand, supply);
 
           for (int c = 0; c < C + 1; c++) {
             Double partial_density = cell_info.partial_densities.get(c);
             if (partial_density == null)
               partial_density = 0.0;
 
             i = x_block_size * k + f_out_position + (C + 1) * prev_id + c;
 
             /*
              * Derivative terms with respect to the partial densities.
              * There are 3 cases but the use of f_out = min (supply,demand)
              * makes a factorization possible
              */
             if (total_density != 0) {
               j = x_block_size * k + (C + 1) * prev_id + c;
               double value = f_out * (total_density - partial_density)
                   / (total_density * total_density);
               assert Numerical.validNumber(value);
               result.setQuick(i, j, value);
             } else {
               /*
                * If the density is zero, any increase of a partial density will
                * increase the corresponding flow by v_i
                */
               j = x_block_size * k + (C + 1) * prev_id + c;
               result.setQuick(i, j, cells[prev_id].getDerivativeDemand(0,
                   delta_t));
             }
 
             /* Derivative terms with respect to supply/demand */
             if (demand < supply || demand == 0) {
               /* The demand is limiting the out-flow */
               assert supply != 0;
               j = x_block_size * k + demand_supply_position + 2 * prev_id;
 
               double value;
               if (demand == 0)
                 continue;
               else
                 value = partial_density / total_density;
               assert Numerical.validNumber(value);
               result.setQuick(i, j, value);
             } else if (supply < demand) {
               /* The supply is limiting the out-flow */
               if (partial_density == 0)
                 continue;
               /* Derivation term with respect to the supply */
               j = x_block_size * k + demand_supply_position + 2 * prev_id + 1;
               double value = partial_density / total_density;
               assert Numerical.validNumber(value);
               result.setQuick(i, j, value);
 
               /* Derivative term with respect to the aggregate split ratios */
               // TODO: Is this needed ?
               /*
                * j = x_block_size * k + aggregate_split_ratios_position
                * + aggregate_index;
                * value = -f_out * partial_density / total_density;
                * assert Numerical.validNumber(value);
                * result.setQuick(i, j, value);
                */
             } else {
               System.out.println("[Warning] Critical point where a" +
                   "derivative dfout is not defined. supply = demand");
             }
 
           }
         }
 
         // Derivative terms for 1xN junctions
       } else if (nb_prev == 1) {
         for (int k = 0; k < T; k++) {
           int in_id = junction.getPrev()[0].getUniqueId();
           CellInfo cell_info = state.profiles[k].getCell(in_id);
           Cell[] next_cells = junction.getNext();
           double total_density = cell_info.total_density;
           double demand = cell_info.demand;
 
           /*
            * We find j such that f_(in_id)_out = min (supply_j /
            * \beta_(in_id)_j)
            */
           int minimum_id_cell = -1; /* The id of the outgoing link */
           int minimum_id_in_next = -1; /* The id in the next array */
           int number_of_minimums = 0; /* Number of time the value is reached */
           double min_supply_over_beta = Double.MAX_VALUE, supply, beta_at_minimum = 0;
           Double beta;
           for (int out = 0; out < next_cells.length; out++) {
             beta = state.profiles[k]
                 .getJunction(junction)
                 .getAggregateSR(in_id, next_cells[out].getUniqueId());
 
             if (beta == null)
               beta = 0.0;
 
             if (beta != 0) {
               supply = state.profiles[k].getCell(next_cells[out]).supply;
               if (supply / beta < min_supply_over_beta) {
                 min_supply_over_beta = supply / beta;
                 minimum_id_cell = next_cells[out].getUniqueId();
                 minimum_id_in_next = out;
                 beta_at_minimum = beta;
                 number_of_minimums = 1;
               } else if (supply / beta == min_supply_over_beta) {
                 number_of_minimums++;
               }
             }
           }
           assert !Double.isInfinite(min_supply_over_beta);
 
           double flow_out;
           if (demand < min_supply_over_beta) {
             flow_out = demand;
             for (int c = 0; c < C + 1; c++) {
               Double partial_density = cell_info.partial_densities.get(c);
               if (partial_density == null)
                 partial_density = 0.0;
 
               i = x_block_size * k + f_out_position + (C + 1) * in_id + c;
 
               /* Derivative with respect to partial densities */
               if (total_density != 0) {
                 j = x_block_size * k + (C + 1) * in_id + c;
                 double value = flow_out * (total_density - partial_density)
                     / (total_density * total_density);
                 assert Numerical.validNumber(value);
                 result.setQuick(i, j, value);
               }
               /*
                * Derivative with respect to supply and demand.
                * There is only the derivative with respect to the demand
                */
               j = x_block_size * k + demand_supply_position + in_id * 2;
 
               double value;
               if (total_density != 0)
                 value = partial_density / total_density;
               else
                 /* Special case where the derivative is not used */
                 value = 66;
               assert Numerical.validNumber(value);
               result.setQuick(i, j, value);
             }
           } else if (min_supply_over_beta < demand) {
             assert total_density != 0;
             flow_out = min_supply_over_beta;
             for (int c = 0; c < C + 1; c++) {
               Double partial_density = cell_info.partial_densities.get(c);
               if (partial_density == null)
                 partial_density = 0.0;
 
               i = x_block_size * k + f_out_position + (C + 1) * in_id + c;
 
               /* Derivative with respect to partial densities */
               j = x_block_size * k + (C + 1) * in_id + c;
               double value = flow_out * (total_density - partial_density)
                   / (total_density * total_density);
               assert Numerical.validNumber(value);
               if (value != 0)
                 result.setQuick(i, j, value);
 
               /*
                * Derivative with respect to supply and aggregate split ratio
                */
               if (number_of_minimums != 1) {
                 System.out.println("More than 1 minimum " + number_of_minimums);
                 continue;
               }
               /* Derivation with respect to the supply */
               if (partial_density != 0) {
                 j = x_block_size * k + demand_supply_position + minimum_id_cell
                     * 2 + 1;
                 value = partial_density / total_density / beta_at_minimum;
                 assert Numerical.validNumber(value);
                 result.setQuick(i, j, value);
 
                 /* Derivative with respect with the aggregate split ratio */
                 /* Computation of the position of the aggregate SR */
                 j = x_block_size * k + aggregate_split_ratios_position
                     + aggregate_index + minimum_id_in_next;
                 value = -flow_out * partial_density
                     / total_density / beta_at_minimum;
                 assert Numerical.validNumber(value);
                 if (value != 0)
                   result.setQuick(i, j, value);
               }
             }
           } else {
             System.out.println("[Warning] Partial derivative not define in "
                 + "1xN junction because supply == demand");
           }
         }
         // Derivative terms for 2x1 junctions
       } else if (nb_prev == 2 && nb_next == 1) {
         System.out.println("2x1 Junction: Not checked yet");
         System.exit(1);
 
         int in_1 = junction.getPrev()[0].getUniqueId();
         int in_2 = junction.getPrev()[1].getUniqueId();
         int out = junction.getNext()[0].getUniqueId();
         double P1 = junction.getPriority(in_1);
         double P2 = junction.getPriority(in_2);
 
         double demand1, demand2, supply, f_in;
         for (int k = 0; k < T; k++) {
           demand1 = state.profiles[k].getCell(in_1).demand;
           demand2 = state.profiles[k].getCell(in_2).demand;
           supply = state.profiles[k].getCell(out).supply;
           f_in = Math.min(demand1 + demand2, supply);
 
           double total_density1 = state.profiles[k].getCell(in_1).total_density;
           double total_density2 = state.profiles[k].getCell(in_2).total_density;
 
           for (int c = 0; c < C + 1; c++) {
 
             double Df_inDdemand1 = 0, Df_inDdemand2 = 0, Df_inDsupply = 0;
             if (demand1 + demand2 < supply) {
               Df_inDdemand1 = 1;
               Df_inDdemand2 = 1;
             }
             if (supply < demand1 + demand2) {
               Df_inDsupply = 1;
             }
 
             /* For the first incoming road in_1 */
             /* Derivative terms with respect to the partial densities */
             Double partial_density = state.profiles[k].getCell(in_1).partial_densities
                 .get(c);
             if (partial_density == null)
               partial_density = 0.0;
 
             if (total_density1 != 0) {
 
               double f_in_1_out;
               double DfDdemand1 = 0, DfDdemand2 = 0, DfDsupply = 0;
               if (P1 * (f_in - demand1) > P2 * demand1) {
                 f_in_1_out = demand1;
                 DfDdemand1 = 1;
               } else if (P1 * demand2 < P2 * (f_in - demand2)) {
                 f_in_1_out = f_in - demand2;
                 DfDdemand1 = Df_inDdemand1;
                 DfDdemand2 = Df_inDdemand2 - 1;
                 DfDsupply = Df_inDsupply;
               } else {
                 f_in_1_out = P1 * f_in;
                 DfDdemand1 = P1 * Df_inDdemand1;
                 DfDdemand2 = P1 * Df_inDdemand2;
                 DfDsupply = P1 * Df_inDsupply;
               }
 
               i = x_block_size * k + f_out_position + in_1
                   * (C + 1) + c;
               j = x_block_size * k + in_1 * (C + 1) + c;
 
               double value = f_in_1_out * (total_density1 - partial_density)
                   / (total_density1 * total_density1);
               result.setQuick(i, j, value);
 
               /* Derivative terms with respect to supply/demand */
               if (partial_density != 0) {
                 if (DfDdemand1 != 0) {
                   j = x_block_size * k + demand_supply_position + in_1 * 2;
                   assert Numerical.validNumber(DfDdemand1);
                   result.setQuick(i, j, DfDdemand1);
                 }
                 if (DfDdemand2 != 0) {
                   j = x_block_size * k + demand_supply_position + in_2 * 2;
                   assert Numerical.validNumber(DfDdemand2);
                   result.setQuick(i, j, DfDdemand2);
                 }
                 if (DfDsupply != 0) {
                   j = x_block_size * k + demand_supply_position + out * 2 + 1;
                   assert Numerical.validNumber(DfDsupply);
                   result.setQuick(i, j, DfDsupply);
                 }
               }
             }
 
             /* For the second incoming road in_2 */
             /* Derivative terms with respect to the partial densities */
             partial_density = state.profiles[k].getCell(in_2).partial_densities
                 .get(c);
             if (partial_density == null)
               partial_density = 0.0;
 
             if (total_density2 != 0) {
 
               double f_in_2_out;
               double DfDdemand1 = 0, DfDdemand2 = 0, DfDsupply = 0;
               if (P2 * (f_in - demand2) > P1 * demand2) {
                 f_in_2_out = demand1;
                 DfDdemand2 = 1;
               } else if (P2 * demand2 < P1 * (f_in - demand1)) {
                 f_in_2_out = f_in - demand1;
                 DfDdemand2 = Df_inDdemand2;
                 DfDdemand1 = Df_inDdemand1 - 1;
                 DfDsupply = Df_inDsupply;
               } else {
                 f_in_2_out = P2 * f_in;
                 DfDdemand2 = P2 * Df_inDdemand2;
                 DfDdemand1 = P2 * Df_inDdemand1;
                 DfDsupply = P2 * Df_inDsupply;
               }
 
               i = x_block_size * k + f_out_position + in_2
                   * (C + 1) + c;
               j = x_block_size * k + in_2 * (C + 1) + c;
 
               double value = f_in_2_out * (total_density2 - partial_density)
                   / (total_density2 * total_density2);
               result.setQuick(i, j, value);
 
               /* Derivative terms with respect to supply/demand */
               if (partial_density != 0) {
                 if (DfDdemand1 != 0) {
                   j = x_block_size * k + demand_supply_position + in_1 * 2;
                   assert Numerical.validNumber(DfDdemand1);
                   result.setQuick(i, j, DfDdemand1);
                 }
                 if (DfDdemand2 != 0) {
                   j = x_block_size * k + demand_supply_position + in_2 * 2;
                   assert Numerical.validNumber(DfDdemand2);
                   result.setQuick(i, j, DfDdemand2);
                 }
                 if (DfDsupply != 0) {
                   j = x_block_size * k + demand_supply_position + out * 2 + 1;
                   assert Numerical.validNumber(DfDsupply);
                   result.setQuick(i, j, DfDsupply);
                 }
               }
             }
           }
 
         }
 
       } else {
         assert false : "[dhdx] Only 1x1, 2x1, and Nx1 junctions are possible";
       }
       aggregate_index += nb_prev * nb_next;
     }
     // endTime= System.currentTimeMillis();
     // System.out.println(endTime - startTime);
     /*********************************************************
      * Derivative terms for the in-flows
      *********************************************************/
     // System.out.print("In-flows: ");
     // startTime= System.currentTimeMillis();
     int commodity, in_id, out_id;
     for (int j_id = 0; j_id < junctions.length; j_id++) {
       for (int k = 0; k < T; k++) {
         junction_SR = internal_SR.get(k, j_id);
 
         /* If there is no split ratios, it means it is a Nx1 junction */
         if (junction_SR == null) {
           assert junctions[j_id].getNext().length == 1;
           prev_length = junctions[j_id].getPrev().length;
           for (int in = 0; in < prev_length; in++) {
             in_id = junctions[j_id].getPrev()[in].getUniqueId();
             out_id = junctions[j_id].getNext()[0].getUniqueId();
             /* Partial derivative with respect to the flow-out(i,c) */
             for (int c = 0; c < C + 1; c++) {
               i = x_block_size * k + f_in_position + out_id * (C + 1) + c;
               j = x_block_size * k + f_out_position + in_id * (C + 1) + c;
 
               result.setQuick(i, j, 1.0);
             }
           }
         } else {
           assert junctions[j_id].getNext().length != 1;
 
           Iterator<Entry<Triplet, Double>> iterator =
               junction_SR.non_compliant_split_ratios
                   .entrySet()
                   .iterator();
           Entry<Triplet, Double> entry;
           while (iterator.hasNext()) {
             entry = iterator.next();
             in_id = entry.getKey().incoming;
             out_id = entry.getKey().outgoing;
             commodity = entry.getKey().commodity;
             assert commodity == 0;
             i = x_block_size * k + f_in_position + out_id * (C + 1) + commodity;
             j = x_block_size * k + f_out_position + in_id * (C + 1) + commodity;
             assert Numerical.validNumber(entry.getValue());
             result.setQuick(i, j, entry.getValue());
           }
 
           iterator =
               junction_SR.compliant_split_ratios
                   .entrySet()
                   .iterator();
           while (iterator.hasNext()) {
             entry = iterator.next();
             in_id = entry.getKey().incoming;
             out_id = entry.getKey().outgoing;
             commodity = entry.getKey().commodity;
             assert commodity != 0;
 
             i = x_block_size * k + f_in_position + out_id * (C + 1) + commodity;
             j = x_block_size * k + f_out_position + in_id * (C + 1) + commodity;
             assert Numerical.validNumber(entry.getValue());
             result.setQuick(i, j, entry.getValue());
           }
         }
       }
     }
 
     // endTime= System.currentTimeMillis();
     // System.out.println(endTime - startTime);
 
     // System.out.print("Diagonal terms: ");
     // startTime= System.currentTimeMillis();
     for (int index = 0; index < x_block_size * T; index++)
       result.setQuick(index, index, -1.0);
     // endTime= System.currentTimeMillis();
     // System.out.println(endTime - startTime);
 
     return result;
   }
 
   /**
    * @brief Computes the derivative dJ/dU
    * @details
    *          The condition \beta >= 0 is already put in the solver (in
    *          AdjointJVM/org.wsj/Optimizers.scala) do there is only one barrier
    *          in J
    */
   @Override
   public DoubleMatrix1D djdu(State state, double[] control) {
 
     DoubleMatrix1D result =
         DoubleFactory1D.dense.make(T * temporal_control_block_size);
 
     int index_in_control = 0;
     double sum_of_split_ratios;
     double[] origin_demands;
     for (int orig = 0; orig < O; orig++) {
       origin_demands = simulator.origin_demands.get(sources[orig]);
 
       Iterator<Integer> it = sources[orig]
           .getCompliant_commodities()
           .iterator();
       while (it.hasNext()) {
         it.next(); // Needed to empty the iterator
         for (int k = 0; k < T; k++) {
 
           double derivative_term = 0;
 
           /*
            * If the demand is null, the cost function is independent of all the
            * split ratios
            */
           if (origin_demands[k] != 0) {
             sum_of_split_ratios = state.sum_of_split_ratios[orig][k];
             assert sum_of_split_ratios > 1 : "The sum of the split ratios ("
                 + sum_of_split_ratios + ") should be >= 1";
 
             derivative_term = epsilon / (1 - sum_of_split_ratios);
             assert Numerical.validNumber(derivative_term);
 
             result.set(k * temporal_control_block_size
                 + index_in_control, derivative_term);
           }
         }
         index_in_control++;
       }
     }
 
     return result;
   }
 
   /**
    * @brief Computes the dJ/dX matrix
    * @details All terms are zero except the ones which are partial derivative in
    *          a partial density
    */
   @Override
   public SparseDoubleMatrix1D djdx(State state, double[] control) {
 
     SparseDoubleMatrix1D result = new SparseDoubleMatrix1D(T * x_block_size);
 
     /* We put 1 when we derivate with respect to a partial density */
     int block_position;
     for (int k = 0; k < T; k++) {
       block_position = k * x_block_size;
       for (int cell_id = 0; cell_id < cells.length; cell_id++) {
         if (cells[cell_id].isSink())
           continue;
         double value = cells[cell_id].getLength();
         for (int c = 0; c < (C + 1); c++) {
           result.setQuick(block_position + (C + 1) * cell_id + c, value);
         }
       }
     }
 
     return result;
   }
 
   /**
    * @brief Forward simulate after having loaded the external split ratios for
    *        compliant commodities
    * @details For now we even put the zero split ratios because we never clear
    *          the split ratios
    */
   @Override
   public State forwardSimulate(double[] control) {
     return forwardSimulate(control, false);
   }
 
   public State forwardSimulate(double[] control, boolean debug) {
 
     IntertemporalOriginsSplitRatios splits = simulator.splits;
 
     int index_in_control = 0;
     int commodity, coordinate;
     double[][] sum_of_split_ratios = new double[O][T];
     for (int orig = 0; orig < O; orig++) {
 
       Iterator<Integer> it = sources[orig]
           .getCompliant_commodities()
           .iterator();
       while (it.hasNext()) {
         commodity = it.next();
         for (int k = 0; k < T; k++) {
           /*
            * Mapping between splits.get(sources[orig], k).get(commodity) and
            * U[k * C + index_in_control]
            */
           coordinate = k * temporal_control_block_size + index_in_control;
           // TODO: Check this !
           /*
            * assert control[coordinate] >= 0 : "The " + coordinate
            * + "-th control (" + control[coordinate] + ") should be positive";
            */
           splits.get(sources[orig], k).
               put(commodity, control[coordinate] * alpha);
           sum_of_split_ratios[orig][k] += control[coordinate];
         }
         index_in_control++;
       }
 
     }
     State state = simulator.run(debug);
     /* At the end we add the sum of the split ratios at the state */
     state.sum_of_split_ratios = sum_of_split_ratios;
 
     return state;
   }
 
   @Override
   public double[] getStartingPoint() {
     return getControl();
   }
 
   /**
    * @details This function imposes that the control is physical (every split
    *          ratio is positive)
    */
   @Override
   public double objective(double[] control) {
     /* Inforces control[i] >= 0, \forall i */
     for (int i = 0; i < control.length; i++)
       if (control[i] < 0)
         return Double.MAX_VALUE;
 
     return objective(forwardSimulate(control), control);
   }
 
   /**
    * @brief Computes the objective function:
    *        \sum_(i,c,k) \rho(i,c,k)
    *        - \sum_{origin o} epsilon2 * ln(\sum \rho(o,c,k) - 1)
    * @details
    *          The condition \beta >= 0 is already put in the solver (in
    *          AdjointJVM/org.wsj/Optimizers.scala) do there is only one barrier
    *          in J
    */
   public double objective(State state, double[] control) {
     double objective = 0;
 
     /*
      * To compute the sum of the densities ON the network, we add the density of
      * all the cells and then remove the density of the sinks
      */
     for (int k = 0; k < T; k++) {
       for (int cell_id = 0; cell_id < cells.length; cell_id++)
         objective += state.profiles[k].getCell(cell_id).total_density;
 
       for (int d = 0; d < destinations.length; d++)
         objective -= state.profiles[k].getCell(destinations[d].getUniqueId()).total_density;
     }
 
     double[] origin_demands;
 
     for (int orig = 0; orig < O; orig++) {
       origin_demands = simulator.origin_demands.get(sources[orig]);
       for (int k = 0; k < T; k++)
         /* If the demand is null, there is no need to set up a barrier */
         if (origin_demands[k] != 0) {
           double diff = state.sum_of_split_ratios[orig][k] - 1;
 
           // if (diff < 0) {
           // System.out.println("Sum of the split ratio at one origin - 1 is "
           // + diff + ". Should be > 0. Aborting.");
           // assert false;
           // }
           objective -= epsilon * Math.log(diff);
           // assert Numerical.validNumber(objective) :
           // "Invalid objective function";
         }
     }
 
     return objective;
   }
 
   public double totalTravelTime(State state, double[] control) {
     double objective = 0;
     /*
      * To compute the sum of the densities ON the network, we add the density of
      * all the cells and then remove the density of the sinks
      */
     for (int k = 0; k < T; k++) {
       for (int cell_id = 0; cell_id < cells.length; cell_id++)
         objective += state.profiles[k].getCell(cell_id).total_density;
 
       for (int d = 0; d < destinations.length; d++)
         objective -= state.profiles[k].getCell(destinations[d].getUniqueId()).total_density;
     }
     return objective;
   }
 
   /**
    * @brief Run the solver to find the optimal allocation of the compliant
    *        split-ratios
    * @details It begins from a not physical initialized compliant split-ratios
    * @return The control vector representing the compliant split-ratios
    */
   public double[] solve() {
 
     // The split ratios has been initialized in the constructor
     /* solve is inherited by AdjointForJava<State> */
     return optimize(getControl());
   }
 
   public void printProperties(State state) {
     System.out.println("[Printing properties of the given state]");
     System.out.println("Total split ratios at the origins through time steps:");
     for (int o = 0; o < O; o++) {
       System.out.print("[Origin " + o + "]");
       for (int k = 0; k < T; k++)
         System.out.print(" " + state.sum_of_split_ratios[o][k] + " ");
       System.out.println();
 
     }
   }
 
   public double getAlpha() {
     return alpha;
   }
 
   public void setAlpha(double alpha) {
     this.alpha = alpha;
   }
 
   private long averageTime(long begin, long end, int nb_cycles) {
     return (end - begin);
   }
 
   public void profileComputationTime() {
 
     long startTime;
     long endTime;
     double[] control = getControl();
     int nb_iterations = 1000;
     System.out.print("Time for " + nb_iterations
         + " iterations of forward simulate... ");
     startTime = System.currentTimeMillis();
 
     for (int i = 0; i < nb_iterations; i++) {
       forwardSimulate(control);
     }
     endTime = System.currentTimeMillis();
     System.out.println(averageTime(startTime, endTime, nb_iterations));
 
     simulator.initializeSplitRatiosForOptimizer();
     State state = forwardSimulate(getControl());
 
     /* J */
     System.out.print("Time for " + nb_iterations
         + " iterations of objective J... ");
     startTime = System.currentTimeMillis();
     for (int i = 0; i < nb_iterations; i++) {
       objective(state, control);
     }
     endTime = System.currentTimeMillis();
     System.out.println(averageTime(startTime, endTime, nb_iterations));
 
     /* dH/dU */
     System.out.print("Time for " + nb_iterations + " iterations of dhdu... ");
 
     startTime = System.currentTimeMillis();
     for (int i = 0; i < nb_iterations; i++) {
       dhdu(state, control);
     }
     endTime = System.currentTimeMillis();
     System.out.println(averageTime(startTime, endTime, nb_iterations));
 
     /* dH/dX */
     System.out.print("Time for " + nb_iterations + " iterations of dhdx... ");
     startTime = System.currentTimeMillis();
     for (int i = 0; i < nb_iterations; i++) {
       dhdx(state, control);
     }
     endTime = System.currentTimeMillis();
     System.out.println(averageTime(startTime, endTime, nb_iterations));
 
     /* dJ/dX */
     System.out.print("Time for " + nb_iterations + " iterations of djdx... ");
     startTime = System.currentTimeMillis();
     for (int i = 0; i < nb_iterations; i++) {
       djdx(state, control);
     }
     endTime = System.currentTimeMillis();
     System.out.println(averageTime(startTime, endTime, nb_iterations));
 
     /* dJ/dU */
     System.out.print("Time for " + nb_iterations + " iterations of djdu... ");
     startTime = System.currentTimeMillis();
     for (int i = 0; i < nb_iterations; i++) {
       djdu(state, control);
     }
     endTime = System.currentTimeMillis();
     System.out.println(averageTime(startTime, endTime, nb_iterations));
   }
 }

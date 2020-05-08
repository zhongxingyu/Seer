 package at.ac.tuwien.infosys.lsdc.scheduler.heuristics;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import at.ac.tuwien.infosys.lsdc.cloud.cluster.IResourceInformation;
 import at.ac.tuwien.infosys.lsdc.cloud.cluster.Resource;
 import at.ac.tuwien.infosys.lsdc.scheduler.matrix.Matrix;
 import at.ac.tuwien.infosys.lsdc.scheduler.matrix.twoDimensional.MatrixHelper;
 import at.ac.tuwien.infosys.lsdc.scheduler.objects.InsourcedJob;
 import at.ac.tuwien.infosys.lsdc.scheduler.objects.Machine;
 import at.ac.tuwien.infosys.lsdc.scheduler.objects.PhysicalMachine;
 import at.ac.tuwien.infosys.lsdc.scheduler.objects.VirtualMachine;
 
 
 public class BestFit<T extends IResourceInformation> {
 
 	private static final Integer NUMBER_OF_ATTRIBUTES = 3;
 
 	ArrayList<T> machines = null;
 
 	public BestFit(T[] machines) {
 		this.machines = new ArrayList<T>(Arrays.asList(machines));
 	}
 
 	public void setRunningMachines(T[] machines) {
 		this.machines = new ArrayList<T>(Arrays.asList(machines));
 	}
 
 	/**
 	 * Calculates the machine, in which the job passed by the argument job
 	 * fits in best
 	 * Expects that all passed machines have enough free resources to host the job (prefiltering necessary)
 	 * @param job the job to be scheduled in a machine
 	 * @return the id of the machine
 	 */
 	public Machine getBestFittingMachine(InsourcedJob job) {
 		
 		int nrOfMachines = machines.size();
 		if (nrOfMachines == 0) {
 			return null;
 		}
 		Resource [] currentUsedResources = new Resource[nrOfMachines];
 		Resource [] totalResources = new Resource[nrOfMachines];
 		LoadMatrix loadMatrix = new LoadMatrix(NUMBER_OF_ATTRIBUTES, nrOfMachines);
 		LoadMatrix allAvailable = new LoadMatrix(NUMBER_OF_ATTRIBUTES, nrOfMachines);
 
 		int machinecount = 0;
 		Resource currentResource;
 		
 		System.out.println("Trying to find best fitting machine");
 		System.out.println("checking " + machines.size() + " machines");
 		for (T currentMachine : machines) {
 			currentResource = currentMachine.getUsedResources();
 
 			currentResource.addJob(job);
 			currentUsedResources[machinecount] = currentResource;
 			totalResources[machinecount] = currentMachine.getTotalResources();
 
 			loadMatrix.addResource(currentResource);
 			allAvailable.addResource(totalResources[machinecount]);
 			machinecount++;
 		}
 		return getBusiestMachine(nrOfMachines, loadMatrix, allAvailable);
 	}
 
 	/**
 	 * returns the best physical machine, the virtual machine @param machine should be moved to
 	 * @param machine the virtual machine to be moved
 	 * @return the physical machine, the vm should be put into
 	 */
 	public Machine getBestFittingMachine(Machine machine){
 		PhysicalMachine pmIdToIgnore = ((VirtualMachine)machine).getHost();
 		machines.remove(pmIdToIgnore);
 
 		int nrOfMachines = machines.size();
		if (nrOfMachines == 0) {
			return null;
		}
 		Resource [] currentUsedResources = new Resource[nrOfMachines];
 		Resource [] totalResources = new Resource[nrOfMachines];
 		int machinecount = 0;
 		Resource currentResource;
 		LoadMatrix loadMatrix = new LoadMatrix(NUMBER_OF_ATTRIBUTES, nrOfMachines);
 		LoadMatrix allAvailable = new LoadMatrix(NUMBER_OF_ATTRIBUTES, nrOfMachines);
 		for (T currentMachine : machines) {
 			currentResource = currentMachine.getUsedResources();
 			currentResource.addVirtualMachine((VirtualMachine)machine);
 			currentUsedResources[machinecount] = currentResource;
 			totalResources[machinecount] = currentMachine.getTotalResources();
 
 			loadMatrix.addResource(currentResource);
 			allAvailable.addResource(totalResources[machinecount]);
 			machinecount++;
 		}
 		return getBusiestMachine(nrOfMachines, loadMatrix, allAvailable);
 	}
 
 	public Machine getBestFittingMachineIgnoreCurrent(InsourcedJob job){
 
 		if (machines.size() < 2) {
 			return null;
 		}
 		machines = new ArrayList<T>(machines);
 		VirtualMachine vmToIgnore = job.getCurrentVirtualMachineEnvironment();
 		machines.remove(vmToIgnore);
 		
 		return getBestFittingMachine(job);
 
 	}
 
 	private Machine getBusiestMachine(int nrOfMachines, LoadMatrix loadMatrix,
 			LoadMatrix allAvailable) {
 		loadMatrix.divElement(allAvailable);
 		Matrix<Double> rowMeans = MatrixHelper.calculateRowMean(loadMatrix);
 
 		if (nrOfMachines == 1) {
 			return (Machine)machines.get(0);
 		}
 
 		//Integer currentBestMachine = totalResources[0].getId();
 		Integer currentBestMachinePosition = 0;
 		Double currentBestFitLoad = MatrixHelper.getElement(rowMeans,0, 0).doubleValue();
 		for (int i = 1; i < nrOfMachines; i++)
 		{
 			Double currentLoadValue = MatrixHelper.getElement(rowMeans, i, 0).doubleValue();
 			//currentBestFitLoad = MatrixHelper.getElement(rowMeans,0, currentBestMachinePosition).doubleValue();
 			if (currentBestFitLoad < currentLoadValue) {
 				currentBestFitLoad = currentLoadValue;
 				currentBestMachinePosition = i;
 			}
 		}
 
 		return (Machine)(machines.get(currentBestMachinePosition));
 	}
 }

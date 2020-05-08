 /**
  * 
  */
 package ec.app.kernel_gp;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import libsvm.SVC_Q_GP;
 import libsvm.svm;
 import libsvm.svm_gp_problem;
 import libsvm.svm_model;
 import libsvm.svm_parameter;
 import libsvm.svm_problem;
 import ec.Individual;
 import ec.gp.GPIndividual;
 import ec.gp.koza.KozaFitness;
 import ec.gp.koza.KozaStatistics;
 import ec.simple.SimpleEvolutionState;
 import ec.util.Output;
 import ec.util.Parameter;
 
 /**
  * @author tomek
  *
  */
 public class SimpleEvolutionStateSVM extends SimpleEvolutionState {
 
 	/**
 	 * 
 	 */
 	public SimpleEvolutionStateSVM() {
 		super();
 	}
 	
     /**
      * @param result
      */
     public void finish(int result) 
         {
         //Output.message("Finishing");
         /* finish up -- we completed. */
         statistics.finalStatistics(this,result);
 		try {
 			testSolution();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         finisher.finishPopulation(this,result);
         exchanger.closeContacts(this,result);
         evaluator.closeContacts(this,result);
         }
     
     public double testSolution() throws IOException
     {
     	double accuracy = 0.0;
     	
     	KozaStatistics st = (KozaStatistics)statistics;
     	GPIndividual bestSoFar = (GPIndividual) (st.getBestSoFar())[0];
 
	    //Parameter train_path = new Parameter("train-file");
 	    Parameter train_test_path = new Parameter("traintest-file");
 	    Parameter validation_path = new Parameter("validation-file");
 	    Parameter output_path_param = new Parameter("output-file");
 
 	    
	    //String trainFilepath = this.parameters.getString(train_path, null);
 	    String trainTestFilepath = this.parameters.getString(train_test_path, null);	    
 	    String validationFilepath = this.parameters.getString(validation_path, null);
 	    String output_file_name = this.parameters.getString(output_path_param, null);
 	    
 	    
 
	    //svm_gp_problem svm_probl_train = Kernel_GP_problem.read_problem(trainFilepath);
 	    svm_gp_problem svm_probl_train_test = Kernel_GP_problem.read_problem(trainTestFilepath);
 	    svm_gp_problem svm_probl_validation = Kernel_GP_problem.read_problem(validationFilepath);
    	((svm_gp_problem)svm_probl_train_test).ind = bestSoFar;
     	((svm_gp_problem)svm_probl_validation).ind = bestSoFar;
    	((svm_gp_problem)svm_probl_train_test).input = new SVMData();
     	((svm_gp_problem)svm_probl_validation).input = new SVMData();
     	
 		Kernel_GP_problem.set_svm_params();
 
 		//TODO: train libsvm once again using the best individual and bot train+test datasets as training dataset    	
     	svm_model model = svm.svm_train(svm_probl_train_test, Kernel_GP_problem.svm_params);
    	
 //    	DataOutputStream output = new DataOutputStream(new PrintStream(new FileOutputStream(new File(output_file_name))));
     	//TODO: set super.output to get statistics from EvolutionState
     	DataOutputStream output = new DataOutputStream(System.out);
     	   	
     	accuracy = libsvm.Svm_predict_gp.predict_problem(svm_probl_validation, model);
 
 		output.writeBytes(((KozaFitness)(bestSoFar.fitness)).adjustedFitness()+" ");
 		output.writeBytes(accuracy*100+" ");
 		output.close();
     	return accuracy;
     }
 }

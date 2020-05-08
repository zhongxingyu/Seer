 package org.tptp.atp;
 
 import org.tptp.model.*;
 import java.util.*;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 /**
  * This class takes a new job from the JobQueueRepository and uses the {@link TheoremProver} interface
  * for executing it. The {@link Result} is then stored in the backend using the various other repositories.
  *
  * @see RepositoryFactory
  */
 public class QueueJobExecutorService {
     private QueueJobRepository jobRepo;
     
     public QueueJobExecutorService() {
         jobRepo = QueueJobRepository.getInstance();
     }
     
     public boolean executeNextJob() {
         QueueJob job = getNextJob();
         if ( job == null ) {
             return false;
         }
         
         try {
             Result result = executeJob(job);
         
             // Store the result / proof
             if ( result.isSuccessfull()) {
                 job.setStatus(QueueJob.STATUS_PROCESSED);
                 job.setMessage("Success!");
                job.setInputText(result.getInputText());
                job.setOutputText(result.getOutputText());
                 jobRepo.update(job);
                 
                 storeProof(job, result);    
             } else {
                 String message = "Unsuccessfull job";
                 if ( result.getException() != null)
                 {
                     Exception exc = result.getException();
                     message = "An error occured: " + exc.getMessage() + "(" + exc.getClass().getName() + ")";
                 }
                 job.setMessage(message);
                 job.setInputText(result.getInputText());
                 job.setOutputText(result.getOutputText());
                 job.setStatus(QueueJob.STATUS_ERROR);
                 jobRepo.update(job);
             }
             
             return true;
         }
         catch (Exception exc)
         {
             StringWriter writer = new StringWriter();
             exc.printStackTrace(new PrintWriter(writer));
             
             job.setMessage("An exception occured: " + exc.getMessage() + "(" + exc.getClass().getName() + ")");
             job.setInputText("No input available");
             job.setOutputText("No output available\n\nException Stacktrace:\n   " + writer.toString());
             job.setStatus(QueueJob.STATUS_ERROR);
             jobRepo.update(job);
             throw new ModelException(exc);
         }
     }
     
     protected void storeProof(final QueueJob job, final Result result) {
         // Check if the prover is a real prover
         if ( !result.getTheoremProver().isProver()) {
             return;
         }
         
         // Store the proof
         final ProofRepository pRepo = ProofRepository.getInstance();
         final FormulaRepository fRepo = FormulaRepository.getInstance();
         final AlgebraRepository aRepo = AlgebraRepository.getInstance();
         
         // We now filter out the algebras, that contain ALL input formulas
         // These algebras are later linked to the proved formulas
         Set<Algebra> algebras = aRepo.getAll();
         Set<Formula> input = fRepo.getFormulasForQueueJob(job.getId());
         final Set<Algebra> finalAlgebras = new HashSet<Algebra>();
         
         algebras: for ( Algebra a : algebras ) {
             Set<Formula> formulas = fRepo.getByAlgebra(a);
             if ( formulas.containsAll(input)) {
                 finalAlgebras.add(a);
             }
         }
         
         // Execute this inside a transaction
         WorkManager.run(new Runnable() { public void run() {
             Proof proof = new Proof();
             for (ProofStep step : result.getProofSteps()) {
                 proof.addProofStep(step);
             }
             for (String key : result.getDetails().keySet()) {
                 proof.setDetail(key, result.getDetails().get(key));
             }
             pRepo.save(proof); // Ok, our proof now has a ID
             
             // Now link it to the queue
             job.setProofId(proof.getId());
             jobRepo.update(job);
             
             // Now, iterate over the used and proves formulas and
             // check if they might already exist and afterwards
             // link them to the proof
             
             // For the used formulas its very sure they already exist, but we check them nontheless.
             for ( Formula used : result.getUsedFormulas()) {
                 Formula f = storeFormula(fRepo, used);
                 pRepo.storeUsage(proof, f);   
             }
             
             for ( Formula proved : result.getProvedFormulas()) {
                 Formula f = storeFormula(fRepo, proved);
                 pRepo.storeProve(proof, f);
                 
                 // Mark the proved formulas as theorems for all possible algebras (see above)
                 for(Algebra algebra : finalAlgebras) {
                     aRepo.link(algebra, f, false);   
                 }
             }
             
             // Mark the job as processed
             job.setStatus(QueueJob.STATUS_PROCESSED);
             jobRepo.update(job);
         }});
         
     }
     
     /**
      * Checks if the given formula.formulaText already exists in the backend and returns it.
      * If not it is stored in the backend.
      */
     protected Formula storeFormula(FormulaRepository repo, Formula f) {
         Formula f2 = repo.getByFormulaText(f.getFormulaText());
         if ( f2 == null ) {
             repo.save(f);
             return f;
         } else {
             return f2;
         }
     }
     
     /**
      * Look up several required objects for calling the {@link TheoremProver} and call the <code>execute()</code> method.
      * @see #findAtp(QueueJob)
      * @see TheoremProver
      * @see FormulaRepository#getFormulasForQueueJob(long)
      * @see Atp#getOptions()
      */
     protected Result executeJob(QueueJob job) {
         Atp atp = findAtp(job);
         
         TheoremProver prover = TheoremProver.findByNameAndVersion(atp.getAtpName(), atp.getAtpVersion());
         if ( prover == null ) {
             throw new AtpException("Prover with name and version \"" + atp.getAtpName() + " / " + atp.getAtpVersion() + "\" not found.");
         }
         
         Properties properties = new Properties();
         properties.putAll(atp.getOptions());
         
         FormulaRepository fRepo = FormulaRepository.getInstance();
         Set<Formula> formulas = fRepo.getFormulasForQueueJob(job.getId());
         
         return prover.execute(properties, job.getGoalFormula(), formulas);
     }
     
     /**
      * Loads the ATP for the given QueueJob.
      */
     protected Atp findAtp(QueueJob job) {
         AtpRepository repo = AtpRepository.getInstance();
         return repo.get(job.getAtpId());
     }
     
     /**
      * Returns the next queueJob from the repository and locks it directly. This method is synchronized,
      * so multiple invocations should never return the same queuejob.
      */
     protected QueueJob getNextJob() {
         synchronized(QueueJobExecutorService.class) {
             QueueJob job = jobRepo.getNextQueueJob();
             if ( job == null ) {
                 return null;
             }
             job.setStatus(QueueJob.STATUS_LOCKED);
             jobRepo.update(job);
             return job;
         }
     }
 }

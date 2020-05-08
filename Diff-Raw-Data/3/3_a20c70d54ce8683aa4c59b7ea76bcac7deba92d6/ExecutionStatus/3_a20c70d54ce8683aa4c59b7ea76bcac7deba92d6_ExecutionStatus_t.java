 package models.tm.test;
 
 /**
  * Execution Status for {@link Instance}, {@link Run} and {@link RunStep}.
` *
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 public enum ExecutionStatus {
 
     NOT_RUN(0, "Not run"), NOT_COMPLETED(1, "Not completed"), PASSED(2, "Passed"), FAILED(3, "Failed");
 
     private String label;
     private Integer position;
 
     private final static ExecutionStatus[] runValues = new ExecutionStatus[]{FAILED, PASSED};
 
     ExecutionStatus(Integer position, String label) {
         this.label = label;
         this.position = position;
     }
 
     public String getLabel() {
         return label;
     }
 
     public Integer getPosition() {
         return position;
     }
 
     @Override
     public String toString() {
         // this is where i18n would take place
         return label;
     }
 
     public static ExecutionStatus[] runValues() {
         return runValues;
     }
 
     public static ExecutionStatus fromPosition(Integer position) {
         for(ExecutionStatus status : ExecutionStatus.values()) {
             if(status.getPosition() == position) {
                 return status;
             }
         }
         throw new RuntimeException("No status for position " + position);
     }
 }

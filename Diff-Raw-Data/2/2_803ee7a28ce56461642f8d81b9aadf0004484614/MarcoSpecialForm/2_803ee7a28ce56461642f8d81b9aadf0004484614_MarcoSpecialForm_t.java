 package marco.lang;
 
 import marco.lang.contracts.Contract;
 
 public abstract class MarcoSpecialForm extends MarcoRunnable {
     public MarcoSpecialForm(Contract contract) {
         super(contract);
     }
 
     @Override
     public String typeName() {
        return "SpecialForm";
     }
 }

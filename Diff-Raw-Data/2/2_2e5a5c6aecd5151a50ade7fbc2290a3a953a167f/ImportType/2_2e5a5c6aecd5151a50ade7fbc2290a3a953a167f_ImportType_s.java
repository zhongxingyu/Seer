 package kabbadi.domain;
 
 import lombok.Getter;
 
 @Getter
 public enum ImportType {
    LOAN_BASIS("Loan Basis"), OUTRIGHT_PURCHASE("Outright Purchase"), FREE_OF_CHARGE("Free of charge");
 
     private String description;
 
     ImportType(String description) {
         this.description = description;
     }
 }

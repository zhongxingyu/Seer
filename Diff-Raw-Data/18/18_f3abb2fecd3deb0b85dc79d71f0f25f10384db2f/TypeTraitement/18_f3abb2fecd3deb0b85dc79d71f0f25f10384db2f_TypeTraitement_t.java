 package org.esupportail.opi.domain.beans.parameters;
 
 import static fj.Bottom.error;
 
 public enum TypeTraitement {
     AccesSelectif("AS", "Accès sélectif") {
         public boolean canDownloadDocument() {
             return true;
         }
     },
     EnAttente("ET", "En Attente") {
         public boolean canDownloadDocument() {
             return false;
         }
     },
     Transfert("TR", "Transfert") {
         public boolean canDownloadDocument() {
             return false;
         }
     },
     ValidationAcquis("VA", "Validation Acquis") {
         public boolean canDownloadDocument() {
             return true;
         }
     };
 
     public final String code, label;
 
     private TypeTraitement(String code, String label) {
         this.code = code;
         this.label = label;
     }
 
     public abstract boolean canDownloadDocument();
 
     public static TypeTraitement fromCode(String code) {
         switch (code) {
             case "AS": return AccesSelectif;
             case "ET": return EnAttente;
             case "TR": return Transfert;
             case "VA": return ValidationAcquis;
             default: throw error(code + " cannot be associated with any existing TypeTraitement");
         }
     }
     // getters for jsf
     public String getCode() { return code; }
     public String getLabel() { return label; }
 }

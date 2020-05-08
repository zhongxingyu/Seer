 package com.esiea.core;
 
 public class Address {
 
    private enum enums {

        Facturation, Livraison
    };
     private String number;
     private String rue;
     private String ville;
     private String cp;
     private String pays;
     private enums nickAddress;
 
     public Address(String number, String rue, String ville, String cp, String pays) {
         this.nickAddress = enums.Facturation;
         this.number = number;
         this.rue = rue;
         this.ville = ville;
         this.cp = cp;
         this.pays = pays;
     }
 
     public Address(String nickaddress, String number, String rue, String ville, String cp, String pays) {
        if (nickaddress.equals("Facturation")) {
            this.nickAddress = enums.Facturation;
        }
        if (nickaddress.equals("Livraison")) {
            this.nickAddress = enums.Livraison;
        }
         this.number = number;
         this.rue = rue;
         this.ville = ville;
         this.cp = cp;
         this.pays = pays;
     }
 
     public String getNickAddress() {
         return nickAddress.toString();
     }
 
     public enums getEnumNickAddress() {
         return nickAddress;
     }
 
     public void setEnumNickAddress(enums nickAddress) {
         this.nickAddress = nickAddress;
     }
 
     public void setNickAddress(String nickaddress) {
         if (nickaddress.equals(enums.Facturation.toString())) {
             this.nickAddress = enums.Facturation;
         }
         if (nickaddress.equals(enums.Livraison.toString())) {
             this.nickAddress = enums.Livraison;
         }
     }
 
     public String getNumber() {
         return number;
     }
 
     public void setNumber(String number) {
         this.number = number;
     }
 
     public String getRue() {
         return rue;
     }
 
     public void setRue(String rue) {
         this.rue = rue;
     }
 
     public String getVille() {
         return ville;
     }
 
     public void setVille(String ville) {
         this.ville = ville;
     }
 
     public String getCp() {
         return cp;
     }
 
     public void setCp(String cp) {
         this.cp = cp;
     }
 
     public String getPays() {
         return pays;
     }
 
     public void setPays(String pays) {
         this.pays = pays;
     }
 
     @Override
     public String toString() {
         return "Address{" + " number=" + number + ", rue=" + rue + ", Ville=" + ville + ", cp=" + cp + ", pays=" + pays + '}';
     }
 
     public void modifyAddress(String number, String rue, String ville, String cp, String pays) {
         this.number = number;
         this.rue = rue;
         this.ville = ville;
         this.cp = cp;
         this.pays = pays;
     }
 
     public void modifyAddress(String nick, String number, String rue, String ville, String cp, String pays) {
         ;
         this.number = number;
         this.rue = rue;
         this.ville = ville;
         this.cp = cp;
         this.pays = pays;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Address other = (Address) obj;
         boolean isSameAddress = true;
         isSameAddress = isSameAddress && nickAddress.equals(other.getNickAddress());
         isSameAddress = isSameAddress && number.equals(other.getNumber());
         isSameAddress = isSameAddress && rue.equals(other.getRue());
         isSameAddress = isSameAddress && ville.equals(other.getVille());
         isSameAddress = isSameAddress && cp.equals(other.getCp());
         isSameAddress = isSameAddress && pays.equals(other.getPays());
         return isSameAddress;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 73 * hash + (this.nickAddress != null ? this.nickAddress.hashCode() : 0);
         hash = 73 * hash + (this.number != null ? this.number.hashCode() : 0);
         hash = 73 * hash + (this.rue != null ? this.rue.hashCode() : 0);
         hash = 73 * hash + (this.ville != null ? this.ville.hashCode() : 0);
         hash = 73 * hash + (this.cp != null ? this.cp.hashCode() : 0);
         hash = 73 * hash + (this.pays != null ? this.pays.hashCode() : 0);
         return hash;
     }
 
     public void update(Address tmpAddr) {
         this.number = tmpAddr.number;
         this.rue = tmpAddr.rue;
         this.pays = tmpAddr.pays;
         this.ville = tmpAddr.ville;
         this.cp = tmpAddr.cp;
     }
 }

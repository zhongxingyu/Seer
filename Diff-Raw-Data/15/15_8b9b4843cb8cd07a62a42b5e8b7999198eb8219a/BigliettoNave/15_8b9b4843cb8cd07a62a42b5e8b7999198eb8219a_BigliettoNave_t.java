 package loveme.takaloa.model; 
 
 import loveme.utility.Currency;
 
 public class BigliettoNave implements java.io.Serializable {
     
     public BigliettoNave() {
         codice = "";
         trasferimento = "";
         passeggero = new Partecipante();
         classe = new TipoPostoNave();
         tariffa = new Currency(0.00);
         tassePortuali = new Currency(0.00);
         tassaPostoAuto = new Currency(0.00);
     }
     
     private String codice, trasferimento;
     private Partecipante passeggero;
     private TipoPostoNave classe;
     private Currency tariffa;
     private Currency tassePortuali;
     private Currency tassaPostoAuto;
 
     public String getCodice() {
         return codice;
     }
 
     public void setCodice(String codice) {
         this.codice = codice;
     }
 
     public TipoPostoNave getClasse() {
         return classe;
     }
 
     public void setClasse(TipoPostoNave classe) {
         this.classe = classe;
     }
 
     public Partecipante getPasseggero() {
         return passeggero;
     }
 
     public void setPasseggero(Partecipante passeggero) {
         this.passeggero = passeggero;
     }
 
     public Currency getTariffa() {
         return tariffa;
     }
 
     public void setTariffa(Currency tariffa) {
         this.tariffa = tariffa;
     }
 
     public Currency getTassaPostoAuto() {
         return tassaPostoAuto;
     }
 
     public void setTassaPostoAuto(Currency tassaPostoAuto) {
         this.tassaPostoAuto = tassaPostoAuto;
     }
 
     public Currency getTassePortuali() {
         return tassePortuali;
     }
 
     public void setTassePortuali(Currency tassePortuali) {
         this.tassePortuali = tassePortuali;
     }
 
     public String getTrasferimento() {
         return trasferimento;
     }
 
     public void setTrasferimento(String trasferimento) {
         this.trasferimento = trasferimento;
     }
 
    public Currency getTotale() {
        Currency totale = new Currency("0.00");
        totale = totale.sum(tariffa);
        totale = totale.sum(tassePortuali);
        totale = totale.sum(tassaPostoAuto);
        return totale;
    }
    
 }
 

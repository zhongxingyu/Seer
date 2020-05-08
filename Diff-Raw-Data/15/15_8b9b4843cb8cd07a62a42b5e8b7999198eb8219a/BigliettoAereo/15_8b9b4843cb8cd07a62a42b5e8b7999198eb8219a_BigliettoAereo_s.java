 package loveme.takaloa.model; 
 
 import loveme.utility.Currency;
 
 public class BigliettoAereo implements java.io.Serializable {
     
     public BigliettoAereo() {
         codice = "";
         volo = "";
         passeggero = new Partecipante();
         classe = new TipoPostoAereo();
         tariffa = new Currency(0.00);
         tasseAeroportuali = new Currency(0.00);
         adeguamentoCarburante = new Currency(0.00);
         tariffaBagagli = new Currency(0.00);
     }
     
     private String codice, volo;
     private Partecipante passeggero;
     private TipoPostoAereo classe;
     private Currency tariffa, tasseAeroportuali, adeguamentoCarburante, tariffaBagagli;
 
     public String getCodice() {
         return codice;
     }
 
     public void setCodice(String codice) {
         this.codice = codice;
     }
 
     public Currency getAdeguamentoCarburante() {
         return adeguamentoCarburante;
     }
 
     public void setAdeguamentoCarburante(Currency adeguamentoCarburante) {
         this.adeguamentoCarburante = adeguamentoCarburante;
     }
 
     public TipoPostoAereo getClasse() {
         return classe;
     }
 
     public void setClasse(TipoPostoAereo classe) {
         this.classe = classe;
     }
 
     public Currency getTariffa() {
         return tariffa;
     }
 
     public void setTariffa(Currency tariffa) {
         this.tariffa = tariffa;
     }
 
     public Currency getTariffaBagagli() {
         return tariffaBagagli;
     }
 
     public void setTariffaBagagli(Currency tariffaBagagli) {
         this.tariffaBagagli = tariffaBagagli;
     }
 
     public Currency getTasseAeroportuali() {
         return tasseAeroportuali;
     }
 
     public void setTasseAeroportuali(Currency tasseAeroportuali) {
         this.tasseAeroportuali = tasseAeroportuali;
     }
 
     public Partecipante getPasseggero() {
         return passeggero;
     }
 
     public void setPasseggero(Partecipante passeggero) {
         this.passeggero = passeggero;
     }
 
     public String getVolo() {
         return volo;
     }
 
     public void setVolo(String volo) {
         this.volo = volo;
     }
     
     public Currency getTotale() {
         Currency totale = new Currency("0.00");
        totale = tariffa.sum(tariffaBagagli);
         totale = totale.sum(tasseAeroportuali);
         totale = totale.sum(adeguamentoCarburante);
         return totale;
     }
     
 }
 

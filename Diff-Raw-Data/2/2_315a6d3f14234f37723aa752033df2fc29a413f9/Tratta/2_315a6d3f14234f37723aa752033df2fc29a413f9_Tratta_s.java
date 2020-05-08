 package gestione_Catalogo.entity;
 
 import gestione_Catalogo.dao.TrattaDAO;
 
 /**
  * @authors 
  * Remo Sperlongano
  * Ivan Torre
  */
 public class Tratta {
 
 	
 	//attributi di istanza
 	private Integer ID;
 	
 	private Ambiente ambiente;
 	private Mezzo mezzo;
 	private String categoria;
 	private Citta partenza;
 	private Citta arrivo;
 	private Via via;
 	
 	private Info info;
 	
 	private Data dataInserimento;
 	
 	
 	public Tratta(Ambiente ambiente, Mezzo mezzo, String categoria, Citta partenza, Citta arrivo, Via via, Info info){
 		
 		//necessito di un ID disponibile dal db
 		TrattaDAO dao = TrattaDAO.getIstanza();
 		
 		this.ambiente = ambiente;
 		this.mezzo = mezzo;
 		this.categoria = categoria;
 		this.partenza = partenza;
 		this.arrivo = arrivo;
 		this.via = via;
 		this.info = info;
 		dataInserimento = new Data();
 		
 		this.info.updateInfo("--- Inserito il " + dataInserimento.stampaGiorno());
 		
 		
 		//salvo tratta su db
		dao.insertAndReturnId(ambiente, mezzo, categoria, partenza, arrivo, via, info, dataInserimento);
 		
 	}
 
 	
 	public Tratta(Integer ID, Ambiente ambiente, Mezzo mezzo, String categoria, Citta partenza, Citta arrivo, Via via, Info info, Data dataInserimento){
 		
 		this.ID = ID;
 		this.ambiente = ambiente;
 		this.mezzo = mezzo;
 		this.categoria = categoria;
 		this.partenza = partenza;
 		this.arrivo = arrivo;
 		this.via = via;
 		this.info = info;
 		
 		this.dataInserimento = dataInserimento;
 
 	}
 
 	
 	public Integer getID() {
 		return ID;
 	}
 
 
 	public void setID(Integer iD) {
 		ID = iD;
 	}
 
 
 	public Ambiente getAmbiente() {
 		return ambiente;
 	}
 
 	public Mezzo getMezzo() {
 		return mezzo;
 	}
 	
 	public String getCategoria(){
 		return categoria;
 	}
 
 	public Citta getPartenza() {
 		return partenza;
 	}
 
 	public Citta getArrivo() {
 		return arrivo;
 	}
 
 	public Via getVia() {
 		return via;
 	}
 
 	public String getInfo() {
 		return info.toString();
 	}
 
 	public void setInfo(Info info) {
 		this.info = info;
 	}
 
 	public Data getDataInserimento() {
 		return dataInserimento;
 	}
 	
 	
 	public boolean verifyExistence(String ambiente, String mezzo, String partenza, String arrivo, String via) {
 		if (this.ambiente.getIDEsternoElemento().equals(ambiente) && this.mezzo.getIDEsternoElemento().equals(mezzo)
 				&& this.partenza.getIDEsternoElemento().equals(partenza) && this.arrivo.getIDEsternoElemento().equals(arrivo) 
 				&& this.via.getIDEsternoElemento().equals(via))
 			return true;
 		return false;
 	}
 
 	
 }

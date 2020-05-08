 /**
  * 
  */
 package gestione_Catalogo.control;
 
 import gestione_Catalogo.entity.Biglietto;
 import gestione_Catalogo.entity.Data;
 import gestione_Catalogo.entity.Offerta;
 import gestione_Catalogo.entity.Prenotazione;
 import gestione_Catalogo.entity.Viaggiatore;
 import gestione_Catalogo.exception.IDEsternoElementoException;
 import gestione_Catalogo.exception.OffertaInesistenteException;
 import gestione_Catalogo.exception.PostiNonSufficientiException;
 import gestione_Catalogo.exception.PrenotazioneInesistenteException;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 
 /**
  * @authors
  * Remo Sperlongano
  * Ivan Torre
  */
 public class ControlloreModificaPrenotazione extends Controllore {
 	
 	public ControlloreModificaPrenotazione(){
 		super();
 	}
 	
 	
 	public void modificaPrenotazione(String ambiente, String mezzo, String partenza, String arrivo, String via, String offertaScelta, String prenotazioneScelta, ArrayList<String> listaNomi, ArrayList<String> listaCognomi, ArrayList<String> listaEmail) throws ParseException, OffertaInesistenteException, PrenotazioneInesistenteException, IDEsternoElementoException, PostiNonSufficientiException{
 		
 		Data dataPartenza = Data.parseTimestamp(offertaScelta);
 		
 		// prendo l'offerta dalla mappa
 		Offerta offerta = catalogo.getOffertaFromMappa(ambiente, mezzo, partenza, arrivo, via, dataPartenza);
 		//controllo disponibilit biglietti
		if (offerta.getPosti() < listaNomi.size()){
			throw new PostiNonSufficientiException("Non ci sono abbastanza Posti disponibili per soddisfare tale prenotazione");
		}
 		
 		// prendo la prenotazione dalla mappa
 		Prenotazione prenotazione = catalogo.getPrenotazioneFromMappa(ambiente, mezzo, partenza, arrivo, via, dataPartenza, prenotazioneScelta);
 		// libero i posti precedentemente prenotati e svuoto la lista dei biglietti
 		offerta.liberaPosti(prenotazione.getListaBiglietti().size());
 		prenotazione.clearListaBiglietti();
 		
 		ArrayList<Biglietto> listaBiglietti = new ArrayList<Biglietto>();
 		
 		while (!listaNomi.isEmpty()){
 			//creo un nuovo biglietto
 			Biglietto b = new Biglietto(prenotazione.getIdPrenotazione(), listaNomi.get(0), listaCognomi.get(0), listaEmail.get(0));
 			listaBiglietti.add(b);
 			
 			//assegno un posto per ogni biglietto creato
 			offerta.assegnaPosti(1); 
 			
 			//elimino gli elementi in listaNomi, listaCognomi, listaMail
 			listaNomi.remove(0);
 			listaCognomi.remove(0);
 			listaEmail.remove(0);
 		}
 		//aggiungo la nuova lista dei biglietti alla prenotazione
 		prenotazione.setListaBiglietti(listaBiglietti);
 		
 		log.aggiornaLogModificaPrenotazione(ambiente, mezzo, partenza, arrivo, via, offertaScelta, prenotazioneScelta);
 	}
 	
 	
 	public ArrayList<ArrayList<String>> getDatiViaggiatoriPerPrenotazione(String ambiente, String mezzo, String partenza, String arrivo, String via, String offerta, String prenotazione) throws OffertaInesistenteException, PrenotazioneInesistenteException, IDEsternoElementoException, ParseException{
 		
 		ArrayList<String> listaNomi = new ArrayList<String>();
 		ArrayList<String> listaCognomi = new ArrayList<String>();
 		ArrayList<String> listaEmail = new ArrayList<String>();
 		
 		Data dataPartenza = Data.parseTimestamp(offerta);
 		
 		Prenotazione p = catalogo.getPrenotazioneFromMappa(ambiente, mezzo, partenza, arrivo, via, dataPartenza, prenotazione);
 
 		ArrayList<Biglietto> listaBiglietti = p.getListaBiglietti();
 		
 		for (int i=0; i<listaBiglietti.size(); i++){
 			Viaggiatore v = listaBiglietti.get(i).getViaggiatore();
 			listaNomi.add(v.getNome());
 			listaCognomi.add(v.getCognome());
 			listaEmail.add(v.getEmail());
 		}
 		
 		ArrayList<ArrayList<String>> listaDati = new ArrayList<ArrayList<String>>();
 		listaDati.add(listaNomi);
 		listaDati.add(listaCognomi);
 		listaDati.add(listaEmail);
 		return listaDati;
 	}
 
 }

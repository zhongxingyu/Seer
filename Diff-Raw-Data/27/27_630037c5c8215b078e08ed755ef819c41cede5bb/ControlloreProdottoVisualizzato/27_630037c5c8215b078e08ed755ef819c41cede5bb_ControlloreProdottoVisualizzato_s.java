 package gestionale.magazzino.controllore.Dipendente;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import gestionale.magazzino.Acquisto;
 import gestionale.magazzino.Dipendente;
 import gestionale.magazzino.Fondo;
 import gestionale.magazzino.Prodotto;
 import gestionale.magazzino.grafica.dipendente.finestre.GraficaDipendente;
 import gestionale.magazzino.grafica.dipendente.finestre.VisualizzaProdotto;
 
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 
 public class ControlloreProdottoVisualizzato{
 	private Prodotto prodotto;
 	private VisualizzaProdotto visualizza_Prodotto;
 	private DateFormat dateFormat;
 	private Date date;
 	private Dipendente dipendente;
 	
 	public ControlloreProdottoVisualizzato(Dipendente dip)
 	{
 		visualizza_Prodotto = new VisualizzaProdotto();
 		dipendente = dip;
 		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		date = new Date();
 	}
 	
 
 	/**
 	 * visualizza il prodotto selezionato dal catalogo
 	 * @param p
 	 */
 
 	public void showProdotto(GraficaDipendente grafica_Dipendente,int x)
 	{
 		JTable tabella = grafica_Dipendente.getTableProdotti();
 		int id = Integer.parseInt(tabella.getValueAt(x, 0).toString());
 		String nome = tabella.getValueAt(x, 1).toString();
 		float prezzo = Float.parseFloat(tabella.getValueAt(x,3).toString());
 		int quantita = Integer.parseInt(tabella.getValueAt(x,2).toString());
 		prodotto = new Prodotto(id,nome,quantita,prezzo);
 		visualizza_Prodotto.init();
 		visualizza_Prodotto.setIDProdotto(""+id);
 		visualizza_Prodotto.setNomeProdotto(nome);
 		visualizza_Prodotto.setPrezzoProdotto(""+prezzo);
 		visualizza_Prodotto.setQuantitaProdotto(""+quantita);
 		grafica_Dipendente.setState(false);
 	}
 	
 	public void gotoCatalogo(ControlloreCatalogo controlloreCatalogo,int x,GraficaDipendente grafica_Dipendente)
 	{
 		visualizza_Prodotto.doClose();
 		controlloreCatalogo.updateCatalogo(grafica_Dipendente,x);
 		
 	}
 	
 	public void controlloOrdine(ControlloreCatalogo controlloreCatalogo,int x,GraficaDipendente grafica_Dipendente,Dipendente dip)
 	{
 		dipendente = dip;
 		int q = visualizza_Prodotto.getQuantitaProdotto();
 		int qins = visualizza_Prodotto.getQuantita();
 		int quantita = q - qins;
 		System.out.println(q);
 		System.out.println("quantita" +quantita);
 		if(qins > 0 && q > 0 && quantita >= 0)
 		{
 			float y = visualizza_Prodotto.getPrezzoProdotto();
 			float z = y*q;
 			String b = visualizza_Prodotto.getFondoScelto();
 			Fondo f = new Fondo();
 			f = gestionale.magazzino.models.Fondo.visualizzaFondo(b);
 			float v = f.getImporto();
 			if(z > v)
 			{
 				JOptionPane.showMessageDialog(visualizza_Prodotto, "Fondo non sufficiente");
 			}
 			else
 			{
 				visualizza_Prodotto.doClose();
 				f.setImporto(f.getImporto()-z);
 				gestionale.magazzino.models.Fondo.cancellaFondo(f.getNome());
 				gestionale.magazzino.models.Fondo.inserisciFondo(f.getNome(),f.getImporto()); //editable
 				gestionale.magazzino.models.Prodotto.modificaQuantitaProdotto(visualizza_Prodotto.getNomeProdotto(), -qins);
 				Acquisto acq = new Acquisto();
 				acq.setIdDipendente(dipendente.getIdDipendente());
 				acq.setNomeDipendente(dipendente.getNome());
 				acq.setIdProdotto(visualizza_Prodotto.getIDProdotto());
 				acq.setNomeProdotto(visualizza_Prodotto.getNomeProdotto());
 				acq.setIdFondo(f.getId_Fondo());
 				acq.setNomeFondo(f.getNome());
 				acq.setQta(qins);
 				acq.setSpesa(z);
 				date = new Date();
 				String data = dateFormat.format(date);
 				acq.setDataAcquisto(data);
 				gestionale.magazzino.models.Acquisto.inserisciAcquisto(acq.getIdDipendente(), acq.getIdProdotto(), acq.getIdFondo(),qins);
 				JOptionPane.showMessageDialog(visualizza_Prodotto, "Prodotto aggiunto al carrello");
 				controlloreCatalogo.initCatalogo();
 				grafica_Dipendente.updateCatalogo(controlloreCatalogo.getCatalogo());
 			}
 		}
 		if(q <= 0)
 		{
 			int i =JOptionPane.showConfirmDialog(visualizza_Prodotto,"Inviare una notifica al responsabile?",null,JOptionPane.YES_NO_OPTION);
 			if(i == 0)
 			{
				String msg = "L'oggetto: "+visualizza_Prodotto.getNomeProdotto()+" non  disponibile in magazzino";
				gestionale.magazzino.models.Notifica.inserisciNotifica(1, 1, msg);
 				JOptionPane.showMessageDialog(visualizza_Prodotto, "Notifica Inviata");
 			}
 		}
 	}
 	
 	public void dispose()
 	{
 		prodotto = null;
 		dateFormat = null;
 		date = null;
 		dipendente = null;
 		visualizza_Prodotto = null;
 	}
 }

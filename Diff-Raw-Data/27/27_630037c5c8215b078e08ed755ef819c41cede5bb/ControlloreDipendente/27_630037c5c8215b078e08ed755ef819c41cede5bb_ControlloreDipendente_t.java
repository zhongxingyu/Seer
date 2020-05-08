 package gestionale.magazzino.controllore.Dipendente;
 
 import javax.swing.JTable;
 
 import gestionale.magazzino.Dipendente;
 import gestionale.magazzino.Prodotto;
 import gestionale.magazzino.grafica.cancelleria.MyModel;
 import gestionale.magazzino.grafica.dipendente.finestre.GraficaDipendente;
 
 public class ControlloreDipendente{
 	
 	private ControlloreAccount controlloreAccount;
 	private ControlloreCarrello controlloreCarrello;
 	private ControlloreCatalogo controlloreCatalogo;
 	private ControlloreProdottoVisualizzato controlloreProdottoVisualizzato;
 	private GraficaDipendente grafica_Dipendente;
 	private Dipendente dipendente;
 	
 	public ControlloreDipendente()
 	{
 		controlloreAccount = new ControlloreAccount();
 		controlloreCarrello = new ControlloreCarrello();
 		controlloreCatalogo = new ControlloreCatalogo();
 		controlloreProdottoVisualizzato = new ControlloreProdottoVisualizzato(dipendente);
 		dipendente = new Dipendente();
 		grafica_Dipendente = new GraficaDipendente();
 	}
 	
 	public void start()
 	{
 		
 		grafica_Dipendente.init(dipendente);
 	}
 	
 	public JTable getTabellaProdotti()
 	{
 		return this.grafica_Dipendente.getTableProdotti();
 	}
 	
 	public void setDipendente(Dipendente dip)
 	{
 		this.dipendente = dip;
 		System.out.println(dipendente.getEmail());
 	}
 	
 	public void doLogout()
 	{
 		dispose();
 	}
 	
 	public void dispose()
 	{
 		controlloreAccount.dispose();
 		controlloreCarrello.dispose();
 		controlloreCatalogo.dispose();
 		controlloreAccount = null;
 		controlloreCarrello = null;
 		controlloreCatalogo = null;
 		controlloreProdottoVisualizzato = null;
 		dipendente = null;
 		grafica_Dipendente.disposeF();
 	}
 	//Account
 	public void showAccount()
 	{
 		controlloreAccount.showAccount(dipendente,grafica_Dipendente);
 	}
 	//Carrello
 	public void showCarrello()
 	{
 		controlloreCarrello.setDipendente(dipendente);
 		controlloreCarrello.showCarrello(grafica_Dipendente);
 	}
 	
 	public void updateCarrello(int x)
 	{
 		controlloreCarrello.updateCarrello(grafica_Dipendente, x);
 	}
 	
 	public MyModel getCarrello()
 	{
 		return controlloreCarrello.getCarrrello();
 	}
 	
 	public void initCarrello()
 	{
 		controlloreCarrello.initCarrello(dipendente);
 	}
 	//Catalogo
 	public void showCatalogo()
 	{
 		controlloreCatalogo.showCatalogo(grafica_Dipendente);
 	}
 	
 	public void initCatalogo()
 	{
 		controlloreCatalogo.initCatalogo();
 	}
 	
 	public void updateCatalogo(int x)
 	{
 		controlloreCatalogo.updateCatalogo(grafica_Dipendente, x);
 	}
 	
 	public MyModel getCatalogo()
 	{
 		return controlloreCatalogo.getCatalogo();
 	}
 	//Prodotto
 	public void showProdotto(int x)
 	{
 		controlloreProdottoVisualizzato.showProdotto(grafica_Dipendente,x);
 	}
 	
 	public void gotoCatalogo(int x)
 	{
 		controlloreProdottoVisualizzato.gotoCatalogo(controlloreCatalogo,x,grafica_Dipendente);
 	}
 	
 	public void controlloOrdine(int x)
 	{
 		controlloreProdottoVisualizzato.controlloOrdine(controlloreCatalogo,x,grafica_Dipendente,dipendente);
		controlloreCarrello.initCarrello(dipendente);
		grafica_Dipendente.updateCarrello(controlloreCarrello.getCarrrello());
	}
	
	//Modifica Prodotto
	
	public void showOrdinato(int x)
	{
		
 	}
 }

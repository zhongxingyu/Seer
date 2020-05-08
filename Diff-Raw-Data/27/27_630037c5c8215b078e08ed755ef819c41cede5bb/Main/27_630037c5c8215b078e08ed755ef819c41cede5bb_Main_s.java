 package gestionale.magazzino;
 
 
 
 import gestionale.magazzino.controllore.ControlloreLogin;
 import gestionale.magazzino.controllore.ControlloreRegistrazione;
 import gestionale.magazzino.controllore.Dipendente.ControlloreDipendente;
 import gestionale.magazzino.controllore.Responsabile.ControlloreResponsabile;
 
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 
 public class Main {
 	
 	private static Connettore conn;
 	private static int a;
 	private static int z;
 	private static ControlloreDipendente controlloreDipendente;
 	private static ControlloreResponsabile controlloreResponsabile;
 	private static ControlloreLogin controlloreLogin;
 	private static ControlloreRegistrazione controlloreRegistrazione;
 	public static void main(String[] args) 
 	{
 		/**
 		 * Metodo per gestire il multithreading nelle finestre swing
 		 */
 		SwingUtilities.invokeLater(new Runnable()
         {
              /**
               * thread principale
               */
             public void run() 
             {
             	
     			//conn = new Connettore();
             	a = 1;
     			z = 0;
     			Connettore.caricadriver();
     			Connettore.collegati();
     			controlloreDipendente = new ControlloreDipendente();
         		controlloreRegistrazione = new ControlloreRegistrazione();
         		controlloreResponsabile = new ControlloreResponsabile();
         		controlloreLogin = new ControlloreLogin(controlloreDipendente,controlloreRegistrazione,controlloreResponsabile);
         		/**
         		 * Overridding del metodo worker,qui vengono eseguiti i thread secondari
         		 */
                 SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>()
                 {
                 	/**
                 	 * thread che vengono eseguiti in background;
                 	 * istruzioni o funzioni che richiedono elaborazioni complesse e di grandi dimensioni devono essere eseguite in questa funzione (esempio: operazioni con vettori dinami e/o file e/o connessioni internet)
                 	 * (bisogna usare all'interno thread.sleep() per evitare che il programma continui la sua esecuzione prima che tutte le operazioni necessarie siano effettuate
                 	 */
                 	protected Boolean doInBackground() throws Exception 
                 	{
                 		
             			controlloreLogin.start();
                 		return true;
                 	};
   			        
                 	/**
                 	 * simile alla funzione doInBackground ma meno performante,potrebbe causare il freeze del programma
                 	 * i risultati elaborati in questa funzione devono poi essere gestiti in un altra (non ricordo il nome xD,comunque provando in passato,non funzionava bene il passaggio di parametri tra queste due funzioni,i risultati non venivano elaborati)
                 	 */
                 	protected void process() 
                 	{
                 	}
                 	/**
                 	 * funzione che viene chiamata alla fine dei cicli presenti in doInBackground (in questo caso,appena il programma parte in quanto non ci sono cicli di controllo di vita del programma (da ottimizzare))
                 	 * in genere in questa funzione si fa il rilascio di tutte le risorse o il salvataggio di dati prima che il programma venga terminato
                 	 */
                 	protected void done() 
                 	{
   	              		
                 		boolean status;
                 		try {
                 			
                 			status = get();
                 		} catch (InterruptedException e) {
                 		} catch (ExecutionException e) {
                 		}catch(NullPointerException awt){
                         }
                 		
                 	}
   				   
                 };
                 /**
                  * worker.execute() fa partire il metodo worker presente in run()
                  * puo essere richiamato per far rieseguire il metodo worker e le funzioni al suo interno
                  */
   		worker.execute();
 		}
       });
 	}
 	
 	/**
 	 *  metodo per la gestione degli eventi
 	 *  l'attributo a viene utilizzato per controllare quale delle due tabelle l'utente sta attualmente visualizzando(catalogo,controllo)
 	 * @param evento
 	 */
 	public void start(String evento)
 	{
 		
 		//Controlli legati alle finestre
 		
 		System.out.println(evento);
 		gestisciLogin(evento);
 		gestisciRegistrazione(evento);
 		gestisciDipendente(evento);
 		gestisciResponsabile(evento);
 		gestisciOrdine(evento);
 		gestisciMagazzino(evento);
 		gestisciNotifiche(evento);
 		gestisciListaDipendentei(evento);
 		
 		//controlli legati alle tabelle
 		/**
 		 * se a == 4 siamo nella lista dipendenti
 		 * invia la riga selezionata al controllo che mostra il dipendente corrispondente
 		 */
 		
 		if(evento.endsWith("4") && a == 4)
 		{
 			String s = evento;
 			int x = 0;
 			x = Integer.parseInt(s.substring(0, s.length()-1));
 			z = x;
 			controlloreResponsabile.showDipendente(x);
 		}	
 		/**
 		 * se a == 3 siamo nelle notifiche
 		 * invia la riga selezionata al controllo che mostra la notifica corrispondente
 		 */
 		if(evento.endsWith("3") && a == 3)
 		{
 			String s = evento;
 			int x = 0;
 			x = Integer.parseInt(s.substring(0, s.length()-1));
 			z = x;
 			controlloreResponsabile.showNotifica(x);
 		}
 		/**
 		 * se a == 2 siamo nel magazzino
 		 * invia la riga selezionata al controllo che mostra il prodotto corrispondente
 		 */
 		if(evento.endsWith("4") && a == 2)
 		{
 			String s = evento;
 			int x = 0;
 			x = Integer.parseInt(s.substring(0, s.length()-1));
 			z = x;
 			controlloreResponsabile.showModificaProdotto(x);
 		}
 		/**
 		 * se a == 1 siamo nel catalogo
 		 * invia la riga selezionata al controllore che mostra il prodotto corrispondente
 		 */
 		if(evento.endsWith("4") && a == 1)
 		{
 			String s = evento;
 			int x = 0;
 			x = Integer.parseInt(s.substring(0, s.length()-1));
 			z = x;
 			controlloreDipendente.showProdotto(x);
 			
 		}
 		/**
 		 * se a == 0 siamo nel carrello
 		 * invia la riga selezionata al controllo che mostra l'ordine corrispondente
 		 */
 		if(evento.endsWith("4") && a == 0)
 		{
 			String s = evento;
 			int x = 0;
 			x = Integer.parseInt(s.substring(0, s.length()-1));
 			z = x;
			//controlloreDipendente.showOrdinato(x);
 		}
 		
 	}
 	
 	
 	public void gestisciLogin(String evento)
 	{
 		switch(evento)
 		{
 			case "Connetti":
 				a = controlloreLogin.isConnected();
 				break;
 			case "Password":
 				a = controlloreLogin.isConnected();
 				break;
 			case "Email":
 				a = controlloreLogin.isConnected();
 			case "Chiudi":
 				controlloreLogin.disconnect();
 				System.exit(0);
 				break;
 			case "Registrati":
 				controlloreLogin.registering();
 				break;
 			default:
 				break;
 		}
 	}
 
 	public void gestisciRegistrazione(String evento)
 	{
 		switch(evento)
 		{
 			case "Indietro":
 				controlloreRegistrazione.dispose();
 				controlloreLogin.start();
 				break;
 			case "Reset":
 				controlloreRegistrazione.resetRegistrazione();
 				break;
 			case "Invio":
 				boolean b = controlloreRegistrazione.registered();
 				if(b)
 				{
 					controlloreLogin.start();
 				}
 				controlloreRegistrazione.dispose();
 				break;
 			case "dispose Registrazione":
 
 				break;
 			default:
 				break;
 		}
 	}
 
 	public void gestisciDipendente(String evento)
 	{
 	
 		switch(evento)
 		{
 			case "Account":
 				controlloreDipendente.showAccount();
 				break;
 			case "Catalogo":
 				a = 1;
 				controlloreDipendente.showCatalogo();
 				break;
 			case "Carrello":
 				a = 0;		
 				controlloreDipendente.showCarrello();
 				break;
 			case "Logout":
 				controlloreDipendente.doLogout();
 				controlloreLogin.start();
 				break;
 			case "Exit":
 				controlloreDipendente.dispose();
 				System.exit(0);
 				break;
 			case "Carrello Dipendente":
 				controlloreDipendente.updateCarrello(z);
 				break;
 			case "Catalogo Dipendente":
 				controlloreDipendente.updateCatalogo(z);
 				break;
 			case "dispose Dipendente":
 				controlloreDipendente.dispose();
 
 				break;
 			default:
 				break;
 		}
 	}
 
 	public void gestisciResponsabile(String evento)
 	{
 		switch(evento)
 		{
 			case "Account Responsabile":
 				controlloreResponsabile.showAccount();
 				break;
 			case "Catalogo Responsabile":
 				a = 2;
 				controlloreResponsabile.showMagazzino();
 				break;
 			case "Aggiungi Prodotto":
 				a = 2;
 				controlloreResponsabile.showOption();
 				break;
 			case "Notifiche Responsabile":
 				a = 3;
 				controlloreResponsabile.showNotifiche();
 				break;
 			case "Lista Dipendenti":
 				a = 4;
 				controlloreResponsabile.showListaDip();
 				break;
 			case "Logout Responsabile":
 				controlloreResponsabile.doLogout();
 				break;
 			case "Exit Responsabile":
 				controlloreResponsabile.dispose();
 				System.exit(0);
 
 				break;
 			case "dispose Responsabile":
 				controlloreResponsabile.dispose();
 
 				break;
 			default:
 				break;
 		}
 	}
 
 	public void gestisciOrdine(String evento)
 	{
 		switch(evento)
 		{
 			case "Ordina":
 				controlloreDipendente.controlloOrdine(z);
 				break;
 			case "Annulla":
 				controlloreDipendente.gotoCatalogo(z);
 				break;
 			default:
 				break;
 		}
 	}
 
 	public void gestisciMagazzino(String evento)
 	{
 		switch(evento)
 		{
 			case "Inserisci Prodotto":
 				controlloreResponsabile.inserisciProdotto();
 				break;
 			case "Annulla Inserimento":
 				controlloreResponsabile.gotoMagazzino(0);
 				break;
 			case "Modifica Prodotto Responsabile":
 				controlloreResponsabile.modificaProdottoResponsabile();
 				break;
 			case "Rimuovi Prodotto Responsabile":
 				controlloreResponsabile.rimuoviProdottoResponsabile();
 				break;
 			case "Annulla Modifica Responsabile":
 				controlloreResponsabile.gotoMagazzino2();
 				break;
 			case "Magazzino Responsabile":
 				controlloreResponsabile.updateMagazzino(z);
 				break;
 			case "Modifica Prodotto":
 				controlloreResponsabile.updateMagazzino(z);
 				break;
 			default:
 				break;
 		}
 	}
 	
 	public void gestisciNotifiche(String evento)
 	{
 		switch(evento)
 		{
 			case "Elimina Notifiche":
 				controlloreResponsabile.eliminaNotifica();
 				break;
 			case "Indietro Notifiche":
 				controlloreResponsabile.gotoNotifiche(z);
 				break;
 			case "Notifiche Responsabile":
 				controlloreResponsabile.updateNotifiche(z);
 				break;
 			default:
 				break;
 		}
 	}
 	
 	public void gestisciListaDipendentei(String evento)
 	{
 		switch(evento)
 		{
 			case "Modifica Dipendente Responsabile":
 				controlloreResponsabile.modificaDipendenteResp();
 				break;
 			case "Rimuovi Dipendente Responsabile":
 				controlloreResponsabile.rimuoviDipendenteResp();
 				break;
 			case "Annulla Dipendente Responsabile":
 				controlloreResponsabile.gotoDipendenti();
 				break;
 			case "Lista":
 				controlloreResponsabile.updateDipendenti(z);
 				break;
 			default:
 				break;
 		}
 	}
 	
 	public void dispose() throws Throwable
 	{
 		super.finalize();
 	}
 }

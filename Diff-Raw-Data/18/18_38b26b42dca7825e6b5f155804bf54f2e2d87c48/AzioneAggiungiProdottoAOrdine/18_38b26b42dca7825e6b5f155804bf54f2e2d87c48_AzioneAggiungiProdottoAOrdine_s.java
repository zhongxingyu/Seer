 package com.bgs_shop.action;
 
 import java.util.List;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import com.bgs_shop.helper.Helper;
 import com.bgs_shop.helper.RigaOrdineHelper;
 import com.bgs_shop.model.Ordine;
 import com.bgs_shop.model.Prodotto;
 import com.bgs_shop.model.RigaOrdine;
 
 public class AzioneAggiungiProdottoAOrdine extends Azione {
 
 	@Override @SuppressWarnings("unchecked")
 	public String esegui(HttpServletRequest request) throws ServletException {
 		HttpSession sessione = request.getSession();
 		Helper helper = new RigaOrdineHelper(request);
 		
 		Ordine ordineCorrente = (Ordine) sessione.getAttribute("ordineCorrente");
 		RigaOrdine rigaCorrente = null;
 		Prodotto prodottoCorrente = null;
 		int quantitaCorrente = Integer.valueOf(request.getParameter("quantita"));
 		boolean trovato = false;
 		
 		if(helper.convalida()){
 			for(Prodotto p: (List<Prodotto>) sessione.getAttribute("prodotti"))
 				if(request.getParameter(Long.toString(p.getCod()))!=null)
 					prodottoCorrente = p;
 		
 			for(RigaOrdine r: ordineCorrente.getRigheOrdine())
 				if(prodottoCorrente.getCod() == r.getProdotto().getCod()){
 					trovato = true;
 					rigaCorrente = r;
 				}	
 			
 			if(trovato){
 				int nuovaQuantita = rigaCorrente.getQuantita() + quantitaCorrente;
 				rigaCorrente.aggiornaRiga(nuovaQuantita);
 			}
 			else
				ordineCorrente.aggiungiRiga(prodottoCorrente, Integer.valueOf(request.getParameter("quantit�")));
 		}
 		
 		sessione.setAttribute("numeroProdotti", ordineCorrente.getNumeroProdotti());
 		return "nuovoOrdine";
 	}
 
 }

 package com.odea;
 
 import com.odea.domain.Pozo;
 
 public class AltaPozoPage extends EditarPozoPage {
 
 	private static final long serialVersionUID = 1L;
 
 	
 	public AltaPozoPage() {
 		
 		Pozo pozo = new Pozo();
 		
 		this.preparePage(pozo);
 	}
 	
 
 	@Override
 	protected void insertarPozo(Pozo pozo) {
 		this.daoService.altaPozo(pozo);
 	}
 	
 }
 

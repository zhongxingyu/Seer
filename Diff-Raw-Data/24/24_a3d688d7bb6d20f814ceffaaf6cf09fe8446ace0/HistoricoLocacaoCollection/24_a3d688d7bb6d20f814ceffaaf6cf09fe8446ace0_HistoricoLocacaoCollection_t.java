 package br.com.locadora.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 public class HistoricoLocacaoCollection extends ArrayList<HistoricoLocacao> implements Serializable{
 	
	public HistoricoLocacao getByCodigoDvdDataLocacao(String codigoDVD, String dataLocacao) {
 		for (HistoricoLocacao hl : this) {
			if(hl.getCodigoDVD().equals(codigoDVD) && hl.getDataLocacaoAsString().equals(dataLocacao) && !hl.isDevolvido()){
 				return hl;
 			}
 		}
 		return null;
 	}	
 }

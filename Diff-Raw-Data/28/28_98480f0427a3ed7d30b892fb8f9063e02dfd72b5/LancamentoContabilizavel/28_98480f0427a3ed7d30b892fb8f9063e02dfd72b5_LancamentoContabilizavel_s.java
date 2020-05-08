 package br.zero.controlefinanceiro.utils;
 
 import java.util.Calendar;
 
 import br.zero.controlefinanceiro.model.Conta;
 import br.zero.controlefinanceiro.model.Lancamento;
 import br.zero.controlefinanceiro.model.modelo.LancamentoModelo;
 
 public class LancamentoContabilizavel implements Contabilizavel {
 
 	private Double saldoOrigem;
 	private Double saldoDestino;
 	private Lancamento lancamento;
 	private LancamentoModelo lancamentoModelo;
 	private Calendar lancamentoModeloData;
 	private Integer lancamentoModeloN;
	
 	public void setLancamentoBase(Lancamento lancamento) {
 		this.lancamento = lancamento;
 		lancamentoModelo = null;
 	}
	
 	public Lancamento getLancamentoBase() {
 		return lancamento;
 	}
	
 	public void setLancamentoModeloBase(LancamentoModelo lancamentoModelo, Calendar dataBase) {
 		this.lancamentoModelo = lancamentoModelo;
 		lancamento = null;
		
 		lancamentoModeloData = (Calendar) dataBase.clone();
		
		lancamentoModeloData.add(Calendar.DAY_OF_MONTH, lancamentoModelo.getDiaVencimento());
 	}
	
 	public LancamentoModelo getLancamentoModeloBase() {
 		return lancamentoModelo;
 	}
	
 	public Integer getId() {
 		return (lancamentoModelo == null) ? lancamento.getId() : lancamentoModelo.getId();
 	}
 
 	public Calendar getData() {
 		return (lancamentoModelo == null) ? lancamento.getData() : lancamentoModeloData;
 	}
 
 	public Integer getN() {
 		if (lancamentoModelo == null) {
 			return lancamento.getN();
 		} else {
 			return lancamentoModeloN;
 		}
 	}
	
 	public void setN(Integer n) {
 		if (lancamentoModelo == null) {
 			return;
 		}
		
 		this.lancamentoModeloN = n;
 	}
 
 	public Conta getContaOrigem() {
 		return (lancamentoModelo == null) ? lancamento.getContaOrigem() : lancamentoModelo.getContaOrigem();
 	}
 
 	public Conta getContaDestino() {
 		return (lancamentoModelo == null) ? lancamento.getContaDestino() : lancamentoModelo.getContaDestino();
 	}
 
 	public Double getValor() {
 		return (lancamentoModelo == null) ? lancamento.getValor() : lancamentoModelo.getValor();
 	}
 
 	public String getObservacao() {
 		return (lancamentoModelo == null) ? lancamento.getObservacao() : lancamentoModelo.getObservacao();
 	}
 
 	public Double getSaldoOrigem() {
 		return saldoOrigem;
 	}
 
 	public void setSaldoOrigem(Double saldoOrigem) {
 		this.saldoOrigem = saldoOrigem;
 	}
 
 	public Double getSaldoDestino() {
 		return saldoDestino;
 	}
 
 	public void setSaldoDestino(Double saldoDestino) {
 		this.saldoDestino = saldoDestino;
 	}
 
 }

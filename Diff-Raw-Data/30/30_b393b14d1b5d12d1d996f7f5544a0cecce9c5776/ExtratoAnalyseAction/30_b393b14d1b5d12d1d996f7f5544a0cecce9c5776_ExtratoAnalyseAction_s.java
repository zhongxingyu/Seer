 package br.zero.controlefinanceiro.actions;
 
 import java.util.List;
 
 import br.zero.controlefinanceiro.commandlineparser.ExtratoAnalyseSwitches;
 import br.zero.controlefinanceiro.model.Conta;
 import br.zero.controlefinanceiro.model.ContaDAO;
 import br.zero.controlefinanceiro.model.ExtratoLine;
 import br.zero.controlefinanceiro.model.ExtratoParser;
 import br.zero.controlefinanceiro.model.Lancamento;
 import br.zero.controlefinanceiro.model.LancamentoDAO;
 import br.zero.controlefinanceiro.model.extrato.ExtratoLancamento;
 import br.zero.controlefinanceiro.model.extrato.ExtratoLancamentoDAO;
 import br.zero.controlefinanceiro.utils.ControleFinanceiroException;
 import br.zero.tinycontroller.Action;
 
 public class ExtratoAnalyseAction implements Action {
 
 	private LancamentoDAO lancamentoDAO;
 
 	private class ExtratoAnalyseException extends ControleFinanceiroException {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -1702349740543718569L;
 
 		public ExtratoAnalyseException(String message) {
 			super("extrato analyse: " + message);
 		}
 
 	}
 
 	@Override
 	public void run(Object param) throws Exception {
 		ExtratoAnalyseSwitches switches = checkParamValid(param);
 
 		Conta banco = getConta(switches.getNomeBanco());
 
 		List<Lancamento> lancamentoSemExtratoList = getLancamentoSemExtratoList();
 
 		List<ExtratoLancamento> extratoLancamentoOrfao = getExtratoLancamentoOrfao();
 
 		ContaDAO contaDAO = new ContaDAO();
 
 		ExtratoParser parser = banco.getParser();
 
 		if (parser == null) {
 			throw new ExtratoAnalyseException("Nenhum parser encontrado para o banco: \"" + banco.getNome() + "\".");
 		}
 
 		for (ExtratoLancamento linhaExtrato : extratoLancamentoOrfao) {
 			StringBuilder status = new StringBuilder();
 
			status.append(linhaExtrato.getOriginal());

 			parser.parse(linhaExtrato.getOriginal());
 
 			ExtratoLine line = parser.getLine();
 
 			Conta contaExtrato = contaDAO.resolveExtratoLine(banco, line.getReferencia());
 
 			if (contaExtrato == null) {
				// TODO O catch all deve ser implementado aqui!
 				status.append("  ==> Referência não resolvida!");
 
 				continue;
 			}
 
 			boolean extratoLineMatched = false;
 
 			for (Lancamento lancamentoSemExtrato : lancamentoSemExtratoList) {
 				Conta contaOrigemEsperada;
 				Conta contaDestinoEsperada;
 
 				if (lancamentoSemExtrato.getValor() > 0) {
 					// Dinheiro saiu da conta
 					contaOrigemEsperada = banco;
 					contaDestinoEsperada = contaExtrato;
 				} else {
 					// Dinheiro entrou na conta
 					contaOrigemEsperada = contaExtrato;
 					contaDestinoEsperada = banco;
 				}
 
 				if (extratoLineMatch(lancamentoSemExtrato, line, contaOrigemEsperada, contaDestinoEsperada)) {
 					extratoLineMatched = true;
 					lancamentoSemExtrato.setExtrato(linhaExtrato);
 
 					if (switches.getRealize()) {
 						lancamentoDAO.alterar(lancamentoSemExtrato);
 					}
					
 					status.append("  ==> " + lancamentoSemExtrato);
 
 					break;
 				}
 
				if (!extratoLineMatched) {
					status.append("  ==> Sem lançamento correspondente.");
				}
 			}
 		}
 	}
 
 	private boolean extratoLineMatch(Lancamento lancto, ExtratoLine line, Conta contaOrigemEsperada, Conta contaDestinoEsperada) {
 		boolean origemOk = lancto.getContaOrigem().equals(contaOrigemEsperada);
 		boolean destinoOk = lancto.getContaDestino().equals(contaDestinoEsperada);
 		boolean valorOk = lancto.getValor().equals(line.getValor());
 
 		return origemOk && destinoOk && valorOk;
 	}
 
 	private Conta getConta(String nomeConta) throws ExtratoAnalyseException {
 		ContaDAO dao = new ContaDAO();
 
 		Conta conta = dao.getByNome(nomeConta);
 
 		if (conta == null) {
 			throw new ExtratoAnalyseException("Conta \"" + nomeConta + "\" não encontrada...");
 		}
 
 		return conta;
 	}
 
 	private List<ExtratoLancamento> getExtratoLancamentoOrfao() {
 		ExtratoLancamentoDAO dao = new ExtratoLancamentoDAO();
 
 		return dao.listarOrfaos();
 	}
 
 	private List<Lancamento> getLancamentoSemExtratoList() {
 		lancamentoDAO = new LancamentoDAO();
 
 		return lancamentoDAO.listarSemExtrato();
 	}
 
 	private ExtratoAnalyseSwitches checkParamValid(Object param) throws ExtratoAnalyseException {
 		if (!(param instanceof ExtratoAnalyseSwitches)) {
 			throw new ExtratoAnalyseException("Parametro deve ser um ExtratoAnalyseSwitches.");
 		}
 
 		ExtratoAnalyseSwitches switches = (ExtratoAnalyseSwitches) param;
 
 		if (switches.getNomeBanco() == null) {
			throw new ExtratoAnalyseException("");
 		}
 
 		return switches;
 	}
 
 }

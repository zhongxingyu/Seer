 package br.zero.controlefinanceiro.actions;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import br.zero.controlefinanceiro.abstractextratoparser.DatedExtratoLancamento;
 import br.zero.controlefinanceiro.abstractextratoparser.ExtratoLancamentoBalance;
 import br.zero.controlefinanceiro.abstractextratoparser.ExtratoLancamentoTransaction;
 import br.zero.controlefinanceiro.abstractextratoparser.ExtratoLineParser;
 import br.zero.controlefinanceiro.abstractextratoparser.ExtratoParsers;
 import br.zero.controlefinanceiro.abstractextratoparser.ParsedExtratoLancamento;
 import br.zero.controlefinanceiro.commandlineparser.ExtratoAnalyseSwitches;
 import br.zero.controlefinanceiro.commandlineparser.ManualReference;
 import br.zero.controlefinanceiro.model.Conta;
 import br.zero.controlefinanceiro.model.ContaDAO;
 import br.zero.controlefinanceiro.model.Lancamento;
 import br.zero.controlefinanceiro.model.LancamentoDAO;
 import br.zero.controlefinanceiro.model.extrato.ExtratoLancamento;
 import br.zero.controlefinanceiro.model.extrato.ExtratoLancamentoDAO;
 import br.zero.controlefinanceiro.utils.AbstractParsedExtratoLancamento;
 import br.zero.controlefinanceiro.utils.ControleFinanceiroException;
 import br.zero.controlefinanceiro.utils.ExtratoLineParserException;
 import br.zero.textgrid.TextGrid;
 import br.zero.textgrid.TextGridColumnAlignment;
 import br.zero.textgrid.TextGridException;
 import br.zero.textgrid.TextGridFormattedColumn;
 import br.zero.textgrid.formatters.ToStringFormatter;
 import br.zero.tinycontroller.Action;
 
 public class ExtratoAnalyseAction implements Action {
 
 	public enum StatusLinha {
 		BALANCE("B"), TRANSACTION("T"), DONT_APPLY("-");
 
 		private String toString;
 
 		private StatusLinha(String toString) {
 			this.toString = toString;
 		}
 
 		@Override
 		public String toString() {
 			return toString;
 		}
 	}
 
 	public enum LancamentoStatus {
 		DONT_APPLY("-"), UPDATE("U"), DELETE("X"), NEW("*"), CONTA_NOT_FOUND("!");
 
 		private String toString;
 
 		private LancamentoStatus(String toString) {
 			this.toString = toString;
 		}
 
 		@Override
 		public String toString() {
 			return toString;
 		}
 	}
 
 	public class InternalManualReference {
 		private Conta conta;
 		private String regex;
 
 		public Conta getConta() {
 			return conta;
 		}
 
 		public void setConta(Conta conta) {
 			this.conta = conta;
 		}
 
 		public String getRegex() {
 			return regex;
 		}
 
 		public void setRegex(String regex) {
 			this.regex = regex;
 		}
 
 	}
 
 	public class ExtratoLineAnalyseResult {
 
 		private StatusLinha statusLinha;
 		private DatedExtratoLancamento extratoLancamento;
 		private Lancamento lancamento;
 		private LancamentoStatus lancamentoStatus;
 
 		public Integer getExtratoID() {
 			if (extratoLancamento instanceof AbstractParsedExtratoLancamento) {
 				return ((AbstractParsedExtratoLancamento) extratoLancamento).getOrigem().getId();
 			} else {
 				return null;
 			}
 		}
 
 		public Integer getLancamentoID() {
 			if (lancamento == null) {
 				return null;
 			} else {
 				return lancamento.getId();
 			}
 		}
 
 		public StatusLinha getStatusLinha() {
 			return statusLinha;
 		}
 
 		public void setLinhaStatus(StatusLinha statusLinha) {
 			this.statusLinha = statusLinha;
 		}
 
 		public DatedExtratoLancamento getExtrato() {
 			return extratoLancamento;
 		}
 
 		public void setExtrato(DatedExtratoLancamento extrato) {
 			this.extratoLancamento = extrato;
 		}
 
 		public String getLancamento() {
 			if (lancamento == null) {
 				return "[null]";
 			}
 			
 			String s = "#" + lancamento.getId() + " " + lancamento.toString();
 
 			if (s.length() > 85) {
 				s = s.substring(0, 82) + "...";
 			}
 
 			return s;
 		}
 
 		public void setLancamento(Lancamento lancamento) {
 			this.lancamento = lancamento;
 		}
 
 		public LancamentoStatus getLancamentoStatus() {
 			return lancamentoStatus;
 		}
 
 		public void setLancamentoStatus(LancamentoStatus lancamentoStatus) {
 			this.lancamentoStatus = lancamentoStatus;
 		}
 
 		public Calendar getOriginalDate() {
 			if (extratoLancamento == null) {
 				return null;
 			} else {
 				return extratoLancamento.getData();
 			}
 		}
 
 		public String getLinhaOriginal() {
 			if (extratoLancamento == null) {
 				return null;
 			} else {
 				return extratoLancamento.getOrigem().getOriginal();
 			}
 		}
 
 	}
 
 	private LancamentoDAO lancamentoDAO;
 	private Conta banco;
 	private ExtratoAnalyseSwitches switches;
 	private List<InternalManualReference> manualRefList;
 
 	private class ExtratoAnalyseException extends ControleFinanceiroException {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -1702349740543718569L;
 
 		public ExtratoAnalyseException(String message) {
 			super("extrato analyse: " + message);
 		}
 
 		public ExtratoAnalyseException(Exception e) {
 			super(e);
 		}
 
 	}
 
 	@Override
 	public void run(Object param) throws ExtratoAnalyseException {
 		switches = checkParamValid(param);
 
 		manualRefList = makeManualRefList();
 
 		banco = getConta(switches.getNomeBanco());
 
 		List<Lancamento> lancamentoSemExtratoList = getLancamentoSemExtratoList();
 
 		List<ExtratoLancamento> extratoLancamentoOrfao = getExtratoLancamentoOrfao(banco);
 
 		if (extratoLancamentoOrfao.size() == 0) {
 			System.out.println();
 			System.out.println("Nenhum item de extrato órfão encontrado para o banco \"" + banco.getNome() + "\".");
 
 			return;
 		}
 
 		TextGrid grid = createGrid();
 		List<ExtratoLineAnalyseResult> statuses;
 
 		try {
 			statuses = makeSync(lancamentoSemExtratoList, extratoLancamentoOrfao);
 		} catch (ExtratoLineParserException e) {
 			throw new ExtratoAnalyseException(e);
 		}
 
 		grid.setValues(statuses);
 
 		try {
 			grid.show();
 		} catch (TextGridException e) {
 			throw new ExtratoAnalyseException(e);
 		}
 
 		System.out.println();
 		System.out.println("Legenda:");
 		System.out.println("  - : Não se aplica.");
 		System.out.println("  U : Update.");
 		System.out.println("  * : Insert.");
 		System.out.println("  X : Delete.");
 		System.out.println("  ! : Conta não encontrada.");
 	}
 
 	private List<InternalManualReference> makeManualRefList() throws ExtratoAnalyseException {
 		List<InternalManualReference> mrl = new ArrayList<InternalManualReference>();
 
 		ContaDAO dao = new ContaDAO();
 
 		for (ManualReference mr : switches.getManualRefList()) {
 			InternalManualReference imr = new InternalManualReference();
 
 			Conta conta = dao.getByNome(mr.getNomeConta());
 
 			if (conta == null) {
 				throw new ExtratoAnalyseException("Referências manuais: conta " + mr.getNomeConta() + "não encontrada.");
 			}
 
 			imr.setConta(conta);
 			imr.setRegex(mr.getRegex());
 
 			mrl.add(imr);
 		}
 
 		return mrl;
 	}
 
 	private List<ExtratoLineAnalyseResult> makeSync(List<Lancamento> lancamentoSemExtratoList, List<ExtratoLancamento> extratoLancamentoOrfao) throws ExtratoLineParserException, ExtratoAnalyseException {
 		List<DatedExtratoLancamento> extratoLines = new ArrayList<DatedExtratoLancamento>();
 
 		makeParser(extratoLancamentoOrfao, extratoLines);
 
 		List<ExtratoLineAnalyseResult> statuses = new ArrayList<ExtratoLineAnalyseResult>();
 
 		ContaDAO contaDAO = new ContaDAO();
 
 		for (int i = 0; i < extratoLines.size(); i++) {
 			ParsedExtratoLancamento extrato = extratoLines.get(i);
 
 			ExtratoLineAnalyseResult analyseResult;
 
 			if (extrato instanceof ExtratoLancamentoBalance) {
 				 analyseResult = syncBalanceLine();
 				 analyseResult.setExtrato((ExtratoLancamentoBalance) extrato);
 			} else if (extrato instanceof ExtratoLancamentoTransaction) {
 				analyseResult = syncTransactionLine(lancamentoSemExtratoList, contaDAO, (ExtratoLancamentoTransaction) extrato);
 				analyseResult.setExtrato((ExtratoLancamentoTransaction) extrato);
 			} else {
 				throw new ExtratoAnalyseException("Classe de linha desconhecida (\"" + extrato + "\")");
 			}
 
 			if (analyseResult != null) {
 				statuses.add(analyseResult);
 			}
 
 			checkLancamentosNaoResolvidos(extratoLines, lancamentoSemExtratoList, i, statuses);
 		}
 
 		return statuses;
 	}
 
 	private void checkLancamentosNaoResolvidos(List<DatedExtratoLancamento> extratoLines, List<Lancamento> lancamentoOrfaoList, int i, List<ExtratoLineAnalyseResult> statuses) {
 		DatedExtratoLancamento extrato = extratoLines.get(i);
 
 		if (i != extratoLines.size() - 1) {
 			Calendar dataAtual = extrato.getData();
 
 			DatedExtratoLancamento proximo = extratoLines.get(i + 1);
 
 			Calendar dataProximo = proximo.getData();
 
 			if ((dataProximo == null) || (!dataAtual.equals(dataProximo))) {
 				addLancamentosNaoResolvidos(dataAtual, dataProximo, lancamentoOrfaoList, statuses);
 			}
 		}
 	}
 
 	private void addLancamentosNaoResolvidos(Calendar dataAtual, Calendar dataProximo, List<Lancamento> lancamentoOrfaoList, List<ExtratoLineAnalyseResult> statuses) {
 		TimeIgnoringComparator comparator = new TimeIgnoringComparator();
 
 		for (Lancamento lancamento : lancamentoOrfaoList) {
 			boolean isPosteriorAtual = comparator.compare(lancamento.getData(), dataAtual) >= 0;
 			boolean anteriorProximo = comparator.compare(lancamento.getData(), dataProximo) < 0;
 			boolean isOrfao = lancamento.getExtrato() == null;
 
 			if (isPosteriorAtual && anteriorProximo && isOrfao) {
 				ExtratoLineAnalyseResult r = new ExtratoLineAnalyseResult();
 
 				r.setLinhaStatus(StatusLinha.DONT_APPLY);
 				r.setLancamentoStatus(LancamentoStatus.DELETE);
 				r.setLancamento(lancamento);
 
 				statuses.add(r);
 			}
 
 		}
 
 	}
 
 	private class ExtratoLinesComparator implements Comparator<DatedExtratoLancamento> {
 
 		@Override
 		public int compare(DatedExtratoLancamento o1, DatedExtratoLancamento o2) {
 			return o1.getData().compareTo(o2.getData());
 		}
 	}
 
 	private void makeParser(List<ExtratoLancamento> extratoLancamentoOrfao, List<DatedExtratoLancamento> extratoLines) throws ExtratoLineParserException, ExtratoAnalyseException {
 		ExtratoLineParser parser = ExtratoParsers.getParser(banco);
 
 		if (parser == null) {
 			throw new ExtratoAnalyseException("Nenhum parser registrado para o banco: \"" + banco.getNome() + "\".");
 		}
 
 		for (ExtratoLancamento el : extratoLancamentoOrfao) {
 			ParsedExtratoLancamento line = parser.parse(el);
 
 			if (line instanceof DatedExtratoLancamento) {
 				extratoLines.add((DatedExtratoLancamento) line);
 			}
 		}
 
 		Collections.sort(extratoLines, new ExtratoLinesComparator());
 	}
 
 	private ExtratoLineAnalyseResult syncBalanceLine() {
 		ExtratoLineAnalyseResult result = new ExtratoLineAnalyseResult();
 		result.setLinhaStatus(StatusLinha.BALANCE);
 		result.setLancamentoStatus(LancamentoStatus.DONT_APPLY);
 
 		return result;
 	}
 
 	private ExtratoLineAnalyseResult syncTransactionLine(List<Lancamento> lancamentoSemExtratoList, ContaDAO contaDAO, ExtratoLancamentoTransaction line) {
 		ExtratoLineAnalyseResult result = new ExtratoLineAnalyseResult();
 		result.setLinhaStatus(StatusLinha.TRANSACTION);
 
 		Conta contaExtrato = resolveReference(contaDAO, line.getReferencia());
 
 		if (contaExtrato == null) {
 			result.setLancamentoStatus(LancamentoStatus.CONTA_NOT_FOUND);
 			return result;
 		}
 
 		TimeIgnoringComparator comparator = new TimeIgnoringComparator();
 		
 		for (Lancamento lancamentoSemExtrato : lancamentoSemExtratoList) {
 			// Ignora as linhas anteriores ao lancamento sem extrato
 			if (comparator.compare(line.getData(), lancamentoSemExtrato.getData()) > 0) {
 				continue;
 			}
 			
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
 
 			if ((lancamentoSemExtrato.getExtrato() == null) && (extratoLineMatch(lancamentoSemExtrato, line, contaOrigemEsperada, contaDestinoEsperada))) {
 				result.setLancamentoStatus(LancamentoStatus.UPDATE);
 
 				lancamentoSemExtrato.setExtrato(line.getOrigem());
 
 				if (switches.getRealize()) {
 					lancamentoDAO.alterar(lancamentoSemExtrato);
 				}
 
 				result.setLancamento(lancamentoSemExtrato);
 
 				return result;
 			}
 
 		}
 
 		// Nenhum lançamento correspondente encontrado
 		result.setLancamentoStatus(LancamentoStatus.NEW);
 
 		Lancamento novoLancamento = new Lancamento();
 
 		result.setLancamento(novoLancamento);
 
 		novoLancamento.setData(line.getData());
 		// TODO Calcular depois. O cálculo será complicado, deverá levar em conta os
 		// N's pré-existentes no banco e os gerados aqui.
 		novoLancamento.setN(-1);
 
 		Conta contaOrigemEsperada;
 		Conta contaDestinoEsperada;
 
 		if (line.getValor() > 0) {
 			// Dinheiro entrou da conta
 			contaOrigemEsperada = contaExtrato;
 			contaDestinoEsperada = banco;
 		} else {
 			// Dinheiro saiu na conta
 			contaOrigemEsperada = banco;
 			contaDestinoEsperada = contaExtrato;
 		}
 
 		novoLancamento.setContaOrigem(contaOrigemEsperada);
 		novoLancamento.setContaDestino(contaDestinoEsperada);
 
 		novoLancamento.setValor(Math.abs(line.getValor()));
 
 		novoLancamento.setExtrato(line.getOrigem());
 
 		if (switches.getRealize()) {
 			lancamentoDAO.inserir(novoLancamento);
 		}
 
 		return result;
 	}
 
 	private Conta resolveReference(ContaDAO contaDAO, String referencia) {
 		// Consulta as referências manuais para resolver a conta
 		for (InternalManualReference imr : manualRefList) {
 			if (referencia.matches(imr.getRegex())) {
 				return imr.getConta();
 			}
 		}
 
 		// Se não conseguiu, vai para as referências automaticas
 		Conta conta = contaDAO.resolveExtratoLine(banco, referencia);
 
 		if (conta != null) {
 			return conta;
 		}
 
 		return null;
 	}
 
 	private TextGrid createGrid() {
 		TextGrid grid = new TextGrid();
 
 		grid.getData().setHeaderSeparatorChar('=');
 		grid.getData().setTitle("Sincronização de Extrato banco \"" + banco.getNome() + "\"");
 
 		TextGridFormattedColumn.createFormattedColumn(grid, "", new ToStringFormatter(""), TextGridColumnAlignment.CENTER, "getStatusLinha");
 		TextGridFormattedColumn.createFormattedColumn(grid, "Date", TextGridFormattedColumn.DATE_FORMATTER, TextGridColumnAlignment.LEFT, "getOriginalDate");
 		TextGridFormattedColumn.createFormattedColumn(grid, "Original", TextGridFormattedColumn.STRING_FORMATTER, TextGridColumnAlignment.LEFT, "getLinhaOriginal");
 		TextGridFormattedColumn.createFormattedColumn(grid, "", new ToStringFormatter(""), TextGridColumnAlignment.CENTER, "getLancamentoStatus");
 		TextGridFormattedColumn.createFormattedColumn(grid, "Lancamento", TextGridFormattedColumn.STRING_FORMATTER, TextGridColumnAlignment.LEFT, "getLancamento");
 
 		return grid;
 	}
 
 	public class TimeIgnoringComparator implements Comparator<Calendar> {
 		public int compare(Calendar c1, Calendar c2) {
 			if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR))
 				return c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);
 			if (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH))
 				return c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);
 			return c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH);
 		}
 	}
 
 	private boolean extratoLineMatch(Lancamento lancto, ExtratoLancamentoTransaction line, Conta contaOrigemEsperada, Conta contaDestinoEsperada) {
 		Calendar dataEsperada = line.getData();
 
 		Comparator<Calendar> comparator = new TimeIgnoringComparator();
 
 		boolean dataOk = comparator.compare(lancto.getData(), dataEsperada) == 0;
 
 		boolean origemOk = lancto.getContaOrigem().equals(contaOrigemEsperada);
 		boolean destinoOk = lancto.getContaDestino().equals(contaDestinoEsperada);
 		boolean valorOk = lancto.getValor().equals(Math.abs(line.getValor()));
 
 		return dataOk && origemOk && destinoOk && valorOk;
 	}
 
 	private Conta getConta(String nomeConta) throws ExtratoAnalyseException {
 		ContaDAO dao = new ContaDAO();
 
 		Conta conta = dao.getByNome(nomeConta);
 
 		if (conta == null) {
 			throw new ExtratoAnalyseException("Conta \"" + nomeConta + "\" não encontrada...");
 		}
 
 		return conta;
 	}
 
 	private List<ExtratoLancamento> getExtratoLancamentoOrfao(Conta banco) {
 		ExtratoLancamentoDAO dao = new ExtratoLancamentoDAO();
 
 		return dao.listarOrfaos(banco);
 	}
 
 	private List<Lancamento> getLancamentoSemExtratoList() {
 		lancamentoDAO = new LancamentoDAO();
 
 		return lancamentoDAO.listarSemExtrato(banco);
 	}
 
 	private ExtratoAnalyseSwitches checkParamValid(Object param) throws ExtratoAnalyseException {
 		if (!(param instanceof ExtratoAnalyseSwitches)) {
 			throw new ExtratoAnalyseException("Parametro deve ser um ExtratoAnalyseSwitches.");
 		}
 
 		ExtratoAnalyseSwitches switches = (ExtratoAnalyseSwitches) param;
 
 		if (switches.getNomeBanco() == null) {
 			throw new ExtratoAnalyseException("Nome do banco deve ser informado.");
 		}
 
 		return switches;
 	}
 
 }

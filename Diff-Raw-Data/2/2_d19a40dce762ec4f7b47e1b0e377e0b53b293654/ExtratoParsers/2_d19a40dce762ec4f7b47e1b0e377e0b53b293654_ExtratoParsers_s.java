 package br.zero.controlefinanceiro.utils;
 
 import java.text.ParseException;
 
 import br.zero.controlefinanceiro.model.ContaDAO;
 import br.zero.controlefinanceiro.model.ExtratoLine;
 import br.zero.controlefinanceiro.model.ExtratoParser;
 
 /**
  * Apenas instancia os parsers de extrato para que possam ser utilizados.
  * 
  * @author Rafael Monico
  * 
  */
 public class ExtratoParsers {
 
 	public static final ExtratoParser ITAU_EXTRATO_PARSER = createItauParser();
 	public static final ExtratoParser SANTANDER_EXTRATO_PARSER = createSantanderParser();
 
 	private static ExtratoParser createItauParser() {
 		ExtratoParser itauParser = new ExtratoParser() {
 
 			private boolean isTransferLine;
 			private ParseException threwException;
 			private ExtratoLine extratoLine;
 
 			@Override
 			public void parse(String line) {
 				String[] fields = line.split("\t");
 
 				if (fields.length > 8) {
 					isTransferLine = false;
 					return;
 				}
 
 				if ("SALDO ANTERIOR".equals(fields[3])) {
 					isTransferLine = false;
 					return;
 				} else if ("S A L D O".equals(fields[3])) {
 					isTransferLine = false;
 					return;
 				} else if ("SDO CTA/APL AUTOMATICAS".equals(fields[3])) {
 					isTransferLine = false;
 					return;
 				}
 
 				ConcreteExtratoLine el = new ConcreteExtratoLine();
 
				el.setReferencia(fields[2]);
 
 				extratoLine = el;
 
 				// String dataStr = fields[0];
 				//
 				// SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
 				//
 				// data = GregorianCalendar.getInstance();
 				//
 				// try {
 				// data.setTime(sdf.parse(dataStr));
 				// } catch (ParseException e) {
 				// threwException = e;
 				// isTransferLine = false;
 				// }
 				//
 				// data.set(Calendar.YEAR,
 				// GregorianCalendar.getInstance().get(Calendar.YEAR));
 
 				isTransferLine = true;
 			}
 
 			@Override
 			public ExtratoLine getLine() {
 				return extratoLine;
 			}
 
 			@Override
 			public Exception getThrewException() {
 				return threwException;
 			}
 
 			@Override
 			public boolean isTransferLine() {
 				return isTransferLine;
 			}
 
 		};
 
 		return itauParser;
 	}
 
 	private static ExtratoParser createSantanderParser() {
 		ExtratoParser santanderParser = new ExtratoParser() {
 
 			private boolean isTransferLine;
 			private ConcreteExtratoLine extratoLine;
 
 			@Override
 			public void parse(String line) {
 				String[] fields = line.split("\t");
 
 				if (fields.length < 3) {
 					isTransferLine = false;
 					return;
 				}
 
 				if ("SALDO ANTERIOR".equals(fields[2])) {
 					isTransferLine = false;
 					return;
 				}
 
 				ConcreteExtratoLine el = new ConcreteExtratoLine();
 
 				el.setReferencia("");
 
 				extratoLine = el;
 
 				isTransferLine = true;
 
 			}
 
 			@Override
 			public ExtratoLine getLine() {
 				return extratoLine;
 			}
 
 			@Override
 			public boolean isTransferLine() {
 				return isTransferLine;
 			}
 
 			@Override
 			public Exception getThrewException() {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 		};
 
 		return santanderParser;
 	}
 
 	public static void registerParsers() {
 		ContaDAO.registerExtratoParser("itau", ExtratoParsers.ITAU_EXTRATO_PARSER);
 		ContaDAO.registerExtratoParser("santander", ExtratoParsers.SANTANDER_EXTRATO_PARSER);
 	}
 
 }

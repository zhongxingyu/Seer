 package com.pilas.sintatico.tabelas;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.usermodel.WorkbookFactory;
 
 import com.pilas.lexico.Token;
 import com.pilas.sintatico.TransicaoDireta;
 import com.pilas.sintatico.TransicaoIndireta;
 
 public class TransicoesMaquinas {
 
 	private static TransicoesMaquinas instance;
 	private static ArrayList<List<TransicaoDireta>> transicoesDiretas = new ArrayList<List<TransicaoDireta>>();
 	private static ArrayList<List<TransicaoIndireta>> transicoesIndiretas = new ArrayList<List<TransicaoIndireta>>();
 	
 	private TransicoesMaquinas() {
 		this.execute();
 	}
 	
 	public static TransicoesMaquinas getInstance(){
 		if(instance==null)
 			instance = new TransicoesMaquinas();
 		return instance;
 	}
 	
 	private void execute() {
 		//Adiciona as transicoes diretas
 		transicoesDiretas.add(new ArrayList<TransicaoDireta>());
 		transicoesDiretas.add(1, importSheetDireta("1.Diretas"));
 		transicoesDiretas.add(2, importSheetDireta("2.Diretas"));
 		transicoesDiretas.add(3, importSheetDireta("3.Diretas"));
 		transicoesDiretas.add(4, importSheetDireta("4.Diretas"));
 		transicoesDiretas.add(5, importSheetDireta("5.Diretas"));
 		transicoesDiretas.add(6, importSheetDireta("6.Diretas"));
 		
 		//Adiciona as transicoes indiretas
 		transicoesIndiretas.add(new ArrayList<TransicaoIndireta>());
 		transicoesIndiretas.add(1, importSheetIndireta("1.Indiretas"));
 		transicoesIndiretas.add(2, importSheetIndireta("2.Indiretas"));
 		transicoesIndiretas.add(3, importSheetIndireta("3.Indiretas"));
 		transicoesIndiretas.add(4, importSheetIndireta("4.Indiretas"));
 		transicoesIndiretas.add(5, importSheetIndireta("5.Indiretas"));
 		transicoesIndiretas.add(6, importSheetIndireta("6.Indiretas"));
 	}
 	
 	private List<TransicaoDireta> importSheetDireta (String sheetName) {
 	    InputStream inp = null;
 	    Workbook wb = null;
 		try {
 			inp = new FileInputStream("./config/input.xlsx");
 			wb = WorkbookFactory.create(inp);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (InvalidFormatException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	    Sheet sheet = wb.getSheet(sheetName);
 	    System.out.println(sheetName);
 	    int lastColumnNum = 1;
 	    try {
 	    	while (!sheet.getRow(0).getCell(lastColumnNum).getStringCellValue().isEmpty()){
 	    		lastColumnNum++;
 	    	}
 	    } catch (NullPointerException e) {
 	    	//Fim das colunas
 	    }
 	    
 	    List<TransicaoDireta> transicoes = new ArrayList<TransicaoDireta>();
 		
 	    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
 	    	Row row = sheet.getRow(i);
 	    	TransicaoDireta td = new TransicaoDireta();
     		
 	    	String columnValue = "";
 	    	String cellValue = "";
 			for (int j = 1; j < lastColumnNum; j++) {
 				columnValue = sheet.getRow(0).getCell(j).getStringCellValue();
 				cellValue = row.getCell(j).getStringCellValue();
 				if (!cellValue.isEmpty()) {
 					
 					//Monta o token correspondente
 					Token t = new Token();
 					Sheet tokens = wb.getSheet("Tokens");
					Integer tipo = null;
					Integer valor = null;
 					for (int k = 1; k < tokens.getLastRowNum(); k++) {
 						if (columnValue.equalsIgnoreCase(tokens.getRow(k).getCell(0).getStringCellValue())) {
 							tipo = (int) tokens.getRow(k).getCell(1).getNumericCellValue();
 							if (tipo == 259) {
 								valor = (int) tokens.getRow(k).getCell(2).getNumericCellValue();
 							} else {
 								valor = null;
 							}
							
							t.setTipo(tipo);
							t.setValor(valor);
							System.out.println("Estado: " + (i-1)
									+ "\nToken (" + tipo + ", " + valor + ")"
									+ "\nProximo Estado: "+ cellValue);
							
							td.setEstadoAtual(i-1);
							td.setTokenRecebido(t);
							td.setProximoEstado(Integer.valueOf(cellValue));
							transicoes.add(td);
							break;
 						}
 					}
 				}
 			}
 	    }
 	    
 	    return transicoes;
 	}
 	
 	private List<TransicaoIndireta> importSheetIndireta (String sheetName) {
 	    InputStream inp = null;
 	    Workbook wb = null;
 		try {
 			inp = new FileInputStream("./config/input.xlsx");
 			wb = WorkbookFactory.create(inp);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (InvalidFormatException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	    Sheet sheet = wb.getSheet(sheetName);
 	    System.out.println(sheetName);
 	    
 	    List<TransicaoIndireta> transicoes = new ArrayList<TransicaoIndireta>();
 		
 	    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
 	    	Row row = sheet.getRow(i);
 	    	
 	    	TransicaoIndireta ti = new TransicaoIndireta();
 	    	ti.setEstadoAtual((int) row.getCell(0).getNumericCellValue());
 	    	ti.setSubMaquina((int) row.getCell(1).getNumericCellValue());
 	    	ti.setEstadoRetorno((int) row.getCell(2).getNumericCellValue());
 	    	
 			System.out.println("Estado: " + ti.getEstadoAtual()
 							+ "\nSubmaquina (" + ti.getSubMaquina() + ")"
 							+ "\nEstado de Retorno: "+ ti.getEstadoRetorno());
 
 			transicoes.add(ti);
 	    }
 	    
 	    return transicoes;
 	}
 	
 	
 	public List<TransicaoDireta> getTransicoesDiretas(int maquina, int estado) {
 		List<TransicaoDireta> resposta = new ArrayList<TransicaoDireta>();
 		for (TransicaoDireta t : transicoesDiretas.get(maquina)) {
 			if(t != null) {
 				if(t.getEstadoAtual() != null) {
 					if(t.getEstadoAtual() == estado) {
 						resposta.add(t);
 					}									
 				}
 			}
 		}
 		
 		return resposta;
 	}
 
 
 	public List<TransicaoIndireta> getTransicoesIndiretas(int maquina, int estado) {
 		List<TransicaoIndireta> resposta = new ArrayList<TransicaoIndireta>();
 		for (TransicaoIndireta t : transicoesIndiretas.get(maquina)) {
 			if(t != null) {
 				if(t.getEstadoAtual() != null) {
 					if(t.getEstadoAtual() == estado) {
 						resposta.add(t);
 					}				
 				}
 			}
 		}
 		
 		return resposta;
 	}
 
 }

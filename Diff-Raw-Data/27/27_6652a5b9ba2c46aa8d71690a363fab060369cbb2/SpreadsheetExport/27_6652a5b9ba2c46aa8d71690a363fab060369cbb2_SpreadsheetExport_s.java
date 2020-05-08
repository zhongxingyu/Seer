 package business.export;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.odftoolkit.odfdom.type.Color;
 import org.odftoolkit.simple.SpreadsheetDocument;
 import org.odftoolkit.simple.style.Border;
 import org.odftoolkit.simple.style.StyleTypeDefinitions.CellBordersType;
 import org.odftoolkit.simple.style.StyleTypeDefinitions.HorizontalAlignmentType;
 import org.odftoolkit.simple.style.StyleTypeDefinitions.SupportedLinearMeasure;
 import org.odftoolkit.simple.style.StyleTypeDefinitions.VerticalAlignmentType;
 import org.odftoolkit.simple.table.Cell;
 import org.odftoolkit.simple.table.Column;
 import org.odftoolkit.simple.table.Row;
 import org.odftoolkit.simple.table.Table;
 
 import persistence.dto.DTO;
 import persistence.dto.Documento;
 import business.EJB.util.EJBUtility;
 
 public class SpreadsheetExport {
 
 	private static String tmpPath;
 	
 	private static void getPath(){
 		tmpPath = "/tmp"; // Jboss
 	}
 	
 	private static double getStringWidth(String s){
		/*
		double width;
		FontMetrics metrics = new FontMetricsExtension(new Font("Arial",Font.PLAIN,10));
		width = metrics.stringWidth(s);
		return (double) (0.264583 * width);
		*/
		return s.length()*2.5D;
 	}
 	
 	public static String generateSpreadsheet(List<DTO> resultSet) throws Exception{
 		
 		SpreadsheetDocument outerDoc = SpreadsheetDocument.newSpreadsheetDocument();
 		String creationDate = new SimpleDateFormat("dd-MM-YYYY").format(new Date());
 		Documento docDTO = null;
 		Table tbl = outerDoc.getSheetByIndex(0);
 		Row row = tbl.appendRow();
 		Column column = tbl.appendColumn();
 		Cell cel = row.getCellByIndex(0);
 		
 		Color grey = new Color("#DDDDDD");
 		Border border = new Border(new Color("#000000"), 0.001, SupportedLinearMeasure.IN);
 		
 		int coluna;
 		
 		getPath();
 		
 		// Primeira linha
 		coluna = 0;
 		row = tbl.getRowByIndex(0);
 		String index[] = {"Código da Caixa/Códice",
 				"Título da Caixa/Códice",
 				"Código do documento",
 				"Título do documento",
 				"Tipo de Documento",
 				"Autor",
 				"Ocupação do autor",
 				"Destinatário",
 				"Ocupação do destinatário",
 				"Local",
 				"Data",
 				"Palavra Chaves",
 				"URL da imagem",
 				"Resumo"};
 
 		
 		for(int i = 0; i < index.length; i++){
 			column = tbl.getColumnByIndex(coluna);
 			column.setWidth(getStringWidth(index[i]));
 			cel = row.getCellByIndex(coluna++);
 			cel.setCellBackgroundColor(grey);
 			cel.setBorders(CellBordersType.ALL_FOUR, border);
 			cel.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
 			cel.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
 			cel.setDisplayText(index[i]);
 		}
 	
 		// Segunda em diante
 		for(int linha = 0; linha < resultSet.size(); linha++){
 			coluna = 0;
 			docDTO = (Documento) resultSet.get(linha);
 			row = tbl.getRowByIndex(linha+1);
 			String cellContent[] = {docDTO.getCodiceCaixa().getCod().replace("-", " - "),
 					docDTO.getCodiceCaixa().getTitulo(),
 					docDTO.getCod().replace("-", " - "),
 					docDTO.getTitulo(),
 					docDTO.getTipoDocumento().getNome(),
 					docDTO.getAutor().getNome(),
 					docDTO.getAutor().getOcupacao(),
 					docDTO.getDestinatario().getNome(),
 					docDTO.getDestinatario().getOcupacao(),
 					docDTO.getLocal(),
					docDTO.getData().getDay() + "-" + docDTO.getData().getMonth() + "-" + docDTO.getData().getYear(),
 					(docDTO.getPalavraChave1() != null ? docDTO.getPalavraChave1().getPalavra() : " ")	+
 						(docDTO.getPalavraChave2() != null ? " - " + docDTO.getPalavraChave2().getPalavra() : " ") + 
 						(docDTO.getPalavraChave3() != null ? " - " + docDTO.getPalavraChave3().getPalavra() : " "),
 					docDTO.getUrl(),
 					docDTO.getResumo()};
 
 			for(int i = 0; i < cellContent.length; i++){
 				column = tbl.getColumnByIndex(coluna);
 				if(getStringWidth(cellContent[i]) > column.getWidth())	column.setWidth(getStringWidth(cellContent[i]));
 				cel = row.getCellByIndex(coluna++);
 				if(linha%2 == 1)	cel.setCellBackgroundColor(grey);
 				cel.setBorders(CellBordersType.ALL_FOUR, border);
				cel.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
 				cel.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
				cel.setDisplayText(cellContent[i]);
 			}
 		}
 
 
 		String filePath = tmpPath + "/rlt_" + EJBUtility.genRandomString(8) + "_" + creationDate + ".ods";
 //		System.out.println(filePath);
 		outerDoc.save(filePath);
 		outerDoc.close();
 		return filePath;
 	}
 
 	/*public static void main(String[] args) throws Exception {
 		getPath();
 		System.out.println(tmpPath);
 	
 		
 		List<DTO> lista = new ArrayList<DTO>();
 //		DocumentEJB busca = new DocumentEJB();
 //		lista = busca.findByKeyWord("gato");
 		
 		lista.add(new Documento("123","teste","local", "http://google.com.br", "resumo",
 				new CodiceCaixa("123","testeCodiceCaixa",1500,1540),
 				new TipoDocumento("tipoDocumento","Descrição"),
 				new Autor("abcaheo aoehaeofgh aeohaeo foehrfaefh aehjioahrf","ocupação1"),
 				new Autor("abcaheo","ocupação2"),
 				new PalavraChave("a",new TemaPalavraChave("tema")),
 				new PalavraChave("a",new TemaPalavraChave("tema")),
 				new PalavraChave("a",new TemaPalavraChave("tema")),
 				new UserAccount("nome", new Profile("profile"), "a@b.com", "abc"),
 				new Date()));
 		
 		SpreadsheetExport.generateSpreadsheet(lista);
 	}*/
 }

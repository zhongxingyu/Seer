 package app;
 import java.util.ArrayList;
 import java.util.List;
 
 import model.Answer;
 import model.Evaluation;
 import model.Model;
 import model.ModelsAndAnswer;
 import model.Question;
 
 import org.apache.poi.hssf.util.HSSFColor;
 
 import excel.ExcelCell;
 import excel.ExcelColumn;
 import excel.ExcelRow;
 import excel.ExcelSheet;
 import excel.ExcelWorkbook;
 
 /**
  * 
  */
 
 /**
  * Evaluation quick-hacked tool for Dehnadi programming evaluation test
  * @author Pavel Janecka, Karel Petranek
  */
 public class EvaluationMain {
 
 	private final String FILE_INPUT_NAME = "data/data_c01.xlsx";
 	private final String FILE_OUTPUT_NAME = "data/data_c01_.xlsx";
 	private final String QUESTION_SHEET_NAME_REGEXP = "Q\\d*";
 	private final String QUESTION_NUMBER_CHAR = "#";
 	private final int QUESTION_NUMBER_COLUMN_INDEX = 0;
 	
 	private final int RESULT_CORNER_INDEX = 0;
 	private final String RESULT_CORNER_STRING = "Code";
 	private final String RESULT_CELL_STRING = "Results";
 	private final String RESULT_SHEET_NAME_REGEXP = "Results.*";
 	
 	private final String FONT_DEFAULT = "default";
 	private final String FONT_RED = "red";
 	
 	/**
 	 * Default constructor
 	 */
 	public EvaluationMain() {
 		List<Question> questions = null;
 		int[] evaluation = null;
 		
 		try {
 			ExcelWorkbook workbook = new ExcelWorkbook(FILE_INPUT_NAME);
 			createCellStyles(workbook);
 			questions = loadQuestions(workbook);
 			for(ExcelSheet sheet : workbook.getSheetsByPattern(RESULT_SHEET_NAME_REGEXP)) {
 				System.out.println("Sheet " + sheet.getName() + " evaluation started");
 				evaluation = resolveAnswersAndCountEvaluation(sheet, questions);
 				writeEvaluationToSheet(workbook, sheet, evaluation);
 			}
 			workbook.write(FILE_OUTPUT_NAME);
 			System.out.println("Done!");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Creates all font to workbook
 	 * @param workbook {@link ExcelWorkbook} instance
 	 */
 	private void createCellStyles(ExcelWorkbook workbook) {
 		workbook.registrFont(HSSFColor.AUTOMATIC.index, false, false, false, FONT_DEFAULT);
 		workbook.registrFont(HSSFColor.RED.index, false, false, false, FONT_RED);
 	}
 
 	/**
 	 * Loads all questions from {@link ExcelWorkbook} instance
 	 * @param workbook {@link ExcelWorkbook} instance
 	 * @return {@link List} of {@link Question} instances
 	 * @throws NumberFormatException when question number have incorrect format
 	 */
 	private List<Question> loadQuestions(ExcelWorkbook workbook) throws NumberFormatException {
 		String tmpCellString = null;
 		int tmpQID = -1;
 		List<Question> questions = new ArrayList<Question>();
 		List<ModelsAndAnswer> answers = null;
 		List<ExcelCell> tmpAnswer = null;
 		List<ExcelCell> tmpModel = null;
 		
 		for(ExcelSheet sheet : workbook.getSheetsByPattern(QUESTION_SHEET_NAME_REGEXP)) {
 			tmpCellString = sheet.stringAt(QUESTION_NUMBER_COLUMN_INDEX, 0);
 			if(tmpCellString.startsWith(QUESTION_NUMBER_CHAR)) {
 				tmpQID = Integer.valueOf(tmpCellString.replace(QUESTION_NUMBER_CHAR, ""));
 				answers = new ArrayList<ModelsAndAnswer>();
 				tmpAnswer = sheet.getColumn(QUESTION_NUMBER_COLUMN_INDEX);
 				tmpModel = sheet.getColumn(QUESTION_NUMBER_COLUMN_INDEX + 1);
 				for(int i = 0; i < tmpAnswer.size() && i < tmpModel.size(); i++) {
 					if(!tmpAnswer.get(i).asString().isEmpty() && !tmpModel.get(i).asString().isEmpty()) {
 						answers.add(new ModelsAndAnswer(new Answer(tmpAnswer.get(i).asString(), tmpQID), Model.parseModels(tmpModel.get(i).asString())));
 					}
 				}
 				questions.add(new Question(tmpQID, answers));
 			} else {
 				System.out.println("Question on sheet " + sheet.getName() + " does not contain appropriate question number on first row");
 			}
 		}
 		return questions;
 	}
 
 	/**
 	 * Resolves answers from selected {@link ExcelSheet} and evaluate selected {@link Question}s
 	 * @param sheet {@link ExcelSheet} instance
 	 * @param questions {@link List} of {@link Question} instances
 	 * @return int array with evaluations
 	 */
 	private int[] resolveAnswersAndCountEvaluation(ExcelSheet sheet, List<Question> questions) {
 		int[] evals = null;
 		Evaluation evaluation = null;
 		List<Answer> answers = null;
 		
 		if(sheet.cellAt(RESULT_CORNER_INDEX, 0).asString().equalsIgnoreCase(RESULT_CORNER_STRING)) {
 			ExcelRow firstRow = sheet.getRow(0);
			evals = new int[firstRow.size()];
 			for(int i = 1; i < firstRow.size(); i++) {
 				evaluation = new Evaluation();
 				answers = getAnswersInColumn(sheet, i);
 				evals[i - 1] = evaluation.evaluate(questions, answers);
 			}
 		} else {
 			System.out.println("First row of result list does not contain \"" + RESULT_CORNER_STRING + "\" on column num " + RESULT_CORNER_INDEX);
 			return new int[0];
 		}
 		return evals;
 	}
 	
 	/**
 	 * Return answers on selected column index
 	 * @param sheet {@link ExcelSheet} instance
 	 * @param idx column index
 	 * @return {@link List} of {@link Answer}
 	 * @throws NumberFormatException when question number have incorrect format
 	 */
 	private List<Answer> getAnswersInColumn(ExcelSheet sheet, int idx) throws NumberFormatException {
		ExcelColumn column = sheet.getColumnWithEmptyCells(idx, 1);
 		List<Answer> answers = new ArrayList<Answer>(column.size());
 		String tmpCellStr = null;
 		
 		for(int i = 0; i < column.size(); i++) {
 			tmpCellStr = sheet.cellAt(RESULT_CORNER_INDEX, i).asString();
 			if(tmpCellStr.startsWith(QUESTION_NUMBER_CHAR)) {
 				answers.add(new Answer(column.get(i).asString(), Integer.valueOf(tmpCellStr.replace(QUESTION_NUMBER_CHAR, ""))));
 			} 
 		}
 		return answers;
 	}
 
 	/**
 	 * Writes evaluate values to selected sheet
 	 * @param workbook {@link ExcelWorkbook} for font register
 	 * @param sheet {@link ExcelWorkbook} instance
 	 * @param evaluation int array with evaluation values
 	 */
 	private void writeEvaluationToSheet(ExcelWorkbook workbook, ExcelSheet sheet, int[] evaluation) {
 		ExcelCell tmpCell = null;
 		ExcelRow tmpRow = null;
 		
 		int resultRowNum = getResultRowNum(sheet);
 		if(resultRowNum != -1) {
 			tmpRow = sheet.createRow(resultRowNum, evaluation.length, 1);
 			for(int i = 0; i < evaluation.length; i++) {
 				tmpCell = tmpRow.get(i + RESULT_CORNER_INDEX);
 				applyCellStyle(workbook, tmpCell, evaluation[i]);
 				tmpCell.setValue(evaluation[i]);
 			}
 		} else {
 			System.out.println("Cannot find result row in result sheet!");
 			return;
 		}
 	}
 	
 	/**
 	 * Resolves appropriate style for evaluation and apply it to cell
 	 * @param workbook {@link ExcelWorkbook} for font register 
 	 * @param tmpCell {@link ExcelCell} instance
 	 * @param value evaluation value
 	 */
 	private void applyCellStyle(ExcelWorkbook workbook, ExcelCell tmpCell, int value) {
 		if(value > 8) tmpCell.setFont(workbook.getRegistredFont(FONT_RED));
 		else tmpCell.setFont(workbook.getRegistredFont(FONT_DEFAULT));
 	}
 
 	/**
 	 * Resolves index of row with results on selected sheet
 	 * @param resultsSheet {@link ExcelSheet} instance
 	 * @return int result row index
 	 */
 	private int getResultRowNum(ExcelSheet resultsSheet) {
 		ExcelColumn column = resultsSheet.getColumn(RESULT_CORNER_INDEX);
 		
 		for(int i = 0; i < column.size(); i++) {
 			if(column.get(i).asString().equalsIgnoreCase(RESULT_CELL_STRING)) return column.get(i).getY();
 		}
 		return -1;
 	}
 
 	/**
 	 * @param args String
 	 */
 	public static void main(String[] args) {
 		new EvaluationMain();
 	}
 
 }

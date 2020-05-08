 package controllers;
 
 import java.util.List;
 
 import models.SuNumber;
 import models.SuNumber.OneNumber;
 import models.SuNumber.RowNumber;
 import models.SudokuForm;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.index;
 import views.html.sudoku.form;
 
 import check.SudokuCheck;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 
 import static play.data.Form.*;
 
 public class Application extends Controller {
 
 	final static Form<SudokuForm> sudoku = form(SudokuForm.class);
 
 	public static Result index() {
 		SudokuForm sudokuForm = new SudokuForm();
 		sudokuForm.refresh();
 
 		return ok(index.render("SudokuTitle", sudokuForm));
 	}
 
 	final static Form<SuNumber> suForm = form(SuNumber.class);
 
 	public static Result sudoku() {
 		SuNumber number = createSuNumber();
 //		number = new SuNumber(new SuNumber.RowNumber(new SuNumber.OneNumber("1"), new SuNumber.OneNumber("2")),
 //				new SuNumber.RowNumber(new SuNumber.OneNumber("3"), new SuNumber.OneNumber("4")));
 		return ok(form.render("", suForm.fill(number)));
 	}
 
 	public static Result check() {
 		Form<SuNumber> filledForm = suForm.bindFromRequest();
 		SuNumber suNumber = filledForm.get();
 
 //		suNumber.out();
 		suNumber.nullToEmpty();
 		
 		String message = "OK";
 		if(SudokuCheck.check(suNumber) == false) {
 			message = "NG";
 		}
 		System.out.println(message);
 //		System.out.println(suNumber.getClass());
 //		SuNumber suNumber = createSuNumber();
 //		suNumber.out();
 		
 		return ok(form.render(message, suForm.fill(suNumber)));
 	}
 
 	static Function<String, OneNumber> toOneNumber = new Function<String, OneNumber>() {
 		@Override
 		public OneNumber apply(String arg0) {
 			return new OneNumber(arg0);
 		}
 	};
 	
 	private static SuNumber createSuNumber() {
 		
 		List<RowNumber> row = Lists.newArrayList();
 		
 		List<String> r1 = Lists.newArrayList("", "7", "", "", "", "8", "4", "", "");
 		row.add(new RowNumber(Collections2.transform(r1, toOneNumber).toArray(new OneNumber[0])));
 		
 		r1 = Lists.newArrayList("4", "6", "", "", "7", "1", "", "", "");
 		row.add(new RowNumber(Collections2.transform(r1, toOneNumber).toArray(new OneNumber[0])));
 		
 		r1 = Lists.newArrayList("", "2", "", "", "5", "4", "", "", "7");
 		row.add(new RowNumber(Collections2.transform(r1, toOneNumber).toArray(new OneNumber[0])));
 		
 		r1 = Lists.newArrayList("9", "5", "", "", "6", "3", "7", "4", "2");
 		row.add(new RowNumber(Collections2.transform(r1, toOneNumber).toArray(new OneNumber[0])));
 		
 		r1 = Lists.newArrayList("7", "4", "", "", "8", "2", "", "", "");
 		row.add(new RowNumber(Collections2.transform(r1, toOneNumber).toArray(new OneNumber[0])));
 		
 		r1 = Lists.newArrayList("", "1", "", "7", "4", "5", "3", "9", "");
 		row.add(new RowNumber(Collections2.transform(r1, toOneNumber).toArray(new OneNumber[0])));
 
 		r1 = Lists.newArrayList("1", "3", "7", "5", "2", "6", "9", "8", "4");
 		row.add(new RowNumber(Collections2.transform(r1, toOneNumber).toArray(new OneNumber[0])));
 		
 		r1 = Lists.newArrayList("", "8", "", "4", "3", "9", "1", "7", "6");
 		row.add(new RowNumber(Collections2.transform(r1, toOneNumber).toArray(new OneNumber[0])));
 		
 		r1 = Lists.newArrayList("6", "9", "4", "8", "", "7", "2", "5", "");
 		row.add(new RowNumber(Collections2.transform(r1, toOneNumber).toArray(new OneNumber[0])));
 		
		return new SuNumber(row.toArray(new RowNumber[0]));
 	}
 }

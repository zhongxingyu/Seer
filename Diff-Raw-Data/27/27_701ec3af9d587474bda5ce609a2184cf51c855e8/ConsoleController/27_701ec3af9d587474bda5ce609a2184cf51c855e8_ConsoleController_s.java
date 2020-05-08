 package org.calculator.controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.calculator.models.IAlgorithme;
 import org.calculator.models.impl.ConsoleModel;
 import org.calculator.models.impl.HeapSortAlgo;
 import org.calculator.models.impl.InsertionSortAlgo;
 import org.calculator.models.impl.MergeSortAlgo;
 import org.calculator.models.impl.Result;
 import org.calculator.models.impl.SelectionSortAlgo;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 
 /**
  * The Controller of the principal functionality of the calculator
  * 
  * @author khalil AMDOUNI
  *
  */
 @Controller
 public class ConsoleController {
 	
 	@RequestMapping(value = "/calculate", method = RequestMethod.GET)
 	public ModelAndView calculate() {
 		ConsoleModel consoleModel = new ConsoleModel();
 		List<IAlgorithme> algos = new ArrayList<IAlgorithme>();
 		algos.add(new HeapSortAlgo("1", "HeapSort"));
 		algos.add(new InsertionSortAlgo("2", "InsertionSort"));
 		algos.add(new MergeSortAlgo("3", "MergeSort"));
 		algos.add(new SelectionSortAlgo("4", "SelectionSort"));
 		consoleModel.setAlgos(algos);
 		return new ModelAndView("console", "consoleModel", consoleModel);
 	}
 	
 	@RequestMapping(value = "/calculate", method = RequestMethod.POST)
 	public ModelAndView calculate(ConsoleModel consoleModel, BindingResult result) {
 		Random rand = new Random();
 		consoleModel.setResults(new ArrayList<Result>());
 		consoleModel.getResults().add(new Result(10, rand.nextInt(10)));
 		consoleModel.getResults().add(new Result(20, rand.nextInt(20)));
 		consoleModel.getResults().add(new Result(30, rand.nextInt(30)));
 		consoleModel.getResults().add(new Result(40, rand.nextInt(40)));
 		consoleModel.getResults().add(new Result(50, rand.nextInt(50)));
 		consoleModel.getResults().add(new Result(60, rand.nextInt(60)));
 		consoleModel.getResults().add(new Result(70, rand.nextInt(70)));
 		consoleModel.getResults().add(new Result(80, rand.nextInt(80)));
 		consoleModel.getResults().add(new Result(90, rand.nextInt(90)));
 		return new ModelAndView("console", "consoleModel", consoleModel);
 	}
 	
 }

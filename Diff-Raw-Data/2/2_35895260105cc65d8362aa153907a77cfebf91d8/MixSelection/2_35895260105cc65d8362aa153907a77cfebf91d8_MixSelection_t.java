 package com.g4.java.selection;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.g4.java.model.Individual;
 
 public class MixSelection implements Selection {
 
 	private List<Selection> selections;
 
 	public MixSelection(List<Selection> selections) {
 		this.selections = selections;
 	}
 
 	@Override
 	public List<Individual> select(List<Individual> population, int generation) {
 		List<Individual> selected = new ArrayList<Individual>();
 		for (Selection selection : selections) {
 			selected.addAll(selection.select(population, generation));
 		}
 
 		return selected;
 	}
 
 	@Override
 	public List<Individual> select(List<Individual> population, int generation,
 			int ggToSelect) {
		List<Individual> selected = new ArrayList<Individual>(ggToSelect);
 		for (Selection selection : selections) {
 			selected.addAll(selection.select(population, generation, ggToSelect));
 		}
 
 		return selected;
 	}
 }

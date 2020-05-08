 package de.htwg.monopoly.entities;
 
 public class Player {
 	private String name;
 	private char figure;
 	private int budget;
	// TODO: ownership
 	private int position;
 
	public Player(String name, char figure, int budget,
 			int position) {
 		this.name = name;
 		this.figure = figure;
 		this.budget = budget;
 		this.position = position;
 	}
 
 	public Player(String name, char figure, int budget) {
		this(name, figure, budget, 1);
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public char getFigure() {
 		return figure;
 	}
 
 	public void setFigure(char figure) {
 		this.figure = figure;
 	}
 
 	public int getBudget() {
 		return budget;
 	}
 
 	public void setBudget(int budget) {
 		this.budget = budget;
 	}
 
 	public int getPosition() {
 		return position;
 	}
 
 	public void setPosition(int position) {
 		this.position = position;
 	}
 
 }

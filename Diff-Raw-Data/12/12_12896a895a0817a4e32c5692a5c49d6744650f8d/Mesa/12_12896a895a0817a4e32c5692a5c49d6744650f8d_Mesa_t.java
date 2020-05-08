 package mpa.manager.bean;
 
 public class Mesa implements Comparable<Mesa> {
 
 	private int id;
 	private MpaConfiguracao mpa;
 	private int numero;
 	private Objectiviano primeiroObjectiviano;
 	private Objectiviano segundoObjectiviano;
 	private String equipe;
 
 	public Mesa(int numero, MpaConfiguracao mpa, Objectiviano primeiroObjectiviano, Objectiviano segundoObjectiviano, String equipe) {
 		if (numero < 1) throw new IllegalArgumentException("Número da mesa não pode ser menor que 1.");
 		if (primeiroObjectiviano == null)
 			throw new IllegalArgumentException("Primeiro Objectiviano não pode ser nulo.");
 
 		this.mpa = mpa;
 		this.numero = numero;
 		setPrimeiroObjectiviano(primeiroObjectiviano);
 		setSegundoObjectiviano(segundoObjectiviano);
 		this.equipe = equipe;
 	}
 	
 	public Mesa(int id, int numero, MpaConfiguracao mpa, Objectiviano primeiroObjectiviano, Objectiviano segundoObjectiviano, String time) {
 		this(numero, mpa, primeiroObjectiviano, segundoObjectiviano, time);
 		this.id = id;
 	}
 
 	public int getNumero() {
 		return numero;
 	}
 
 	public Objectiviano getPrimeiroObjectiviano() {
 		return primeiroObjectiviano;
 	}
 
 	public void setPrimeiroObjectiviano(Objectiviano primeiroObjectiviano) {
 		this.primeiroObjectiviano = primeiroObjectiviano;
 	}
 
 	public Objectiviano getSegundoObjectiviano() {
 		return segundoObjectiviano;
 	}
 
 	public void setSegundoObjectiviano(Objectiviano segundoObjectiviano) {
 		this.segundoObjectiviano = segundoObjectiviano;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public MpaConfiguracao getMpa() {
 		return mpa;
 	}
 	
 	public String getEquipe() {
 		return equipe;
 	}
 	
 	public void setEquipe(String equipe) {
 		this.equipe = equipe;	
 	}
 
 	@Override
 	public String toString() {
		return numero + " | " + equipe + " -- " + getDevsString();
 	}
 
 	public String getDevsString() {
 		return primeiroObjectiviano + (segundoObjectiviano != null ? " / " + segundoObjectiviano : "");
 	}
 
 	public int compareTo(Mesa o) {
 		if (getNumero() == o.getNumero())
 			return 0;
 		return getNumero() > o.getNumero() ? 1 : -1;
 	}
 
 	public boolean equals(Mesa obj) {
 		return getId() == obj.getId();
 	}
 }

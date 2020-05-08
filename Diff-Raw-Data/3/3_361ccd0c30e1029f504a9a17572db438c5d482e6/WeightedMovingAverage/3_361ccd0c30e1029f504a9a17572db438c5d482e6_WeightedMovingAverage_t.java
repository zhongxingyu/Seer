 package br.com.while42.argentum.indicators;
 
 import br.com.while42.argentum.model.TimeSeries;
 
 public class WeightedMovingAverage implements Indicator {
 	private int totalDays = 2;
 
	public WeightedMovingAverage() {		
	}
	
 	public WeightedMovingAverage(int totalDays) {
 		this.totalDays = totalDays;
 	}
 	
 	@Override
 	public double calcule(int position, TimeSeries serie) {
 		double sum = 0.0;
 		int weight = 1;
 
 		for (int i = position - totalDays; i <= position; i++) {
 			sum += serie.getCandle(i).getLast() * weight;
 			weight++;
 		}
 
 		// Exemplo: 6 = soma dos pesos no intervalo de 3 dias (3 + 2 + 1)
 		
 		int pa = 6; // TODO: pa = (Ai + An) * n / 2
 		return sum / pa;
 	}
 
 }

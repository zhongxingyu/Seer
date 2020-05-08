 package br.com.fourlinux.videostore;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 
 public class MovieStatisticBean {
	private Movie movie;
 	private float rate;
 	
 	private MovieBean movieBean;
 	
 	public void rateMovie(ActionEvent event) {
 		if (movie.getStatistics() != null) {
 			movie.setStatistics(new MovieStatistics());
 		}
 		MovieStatistics statistics = movie.getStatistics();
 		
 		float oldTotalRate = statistics.getRate() * statistics.getTotalVotes();
 		float newTotalRate = oldTotalRate + rate;
 		
 		statistics.setTotalVotes(statistics.getTotalVotes() + 1);
 		float newRate = newTotalRate / statistics.getTotalVotes();
 		
 		statistics.setRate(newRate);
 		
 		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Avaliação registrada com sucesso!", null);
 		FacesContext.getCurrentInstance().addMessage(null, message);
 	}
 	
 	
 	public void voteAsFavorite(ActionEvent event) {
 		MovieStatistics statistics = movie.getStatistics();
 	}
 }

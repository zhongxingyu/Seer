 package controller;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import org.springframework.scheduling.annotation.Async;
 
 import rest.RequestService;
 import aic12.project3.common.beans.SentimentProcessingRequest;
 import aic12.project3.common.beans.SentimentRequest;
 import aic12.project3.common.enums.REQUEST_QUEUE_STATE;
 
 @SuppressWarnings("serial")
 @ManagedBean
 @SessionScoped
 public class RequestController implements Serializable{
 
 	private UUID id;
 	private SentimentRequest response;
 	private String companyName;
 	private Date from;
 	private Date to;
 	private long numberOfTweets;
 	private double minimumSentiment;
 	private double maximumSentiment;
 	private double sentiment;
 		
 	private RequestService requestService;
 
 	private UUID generateId() {
 		return UUID.randomUUID();
 	};
 	
 	@Async
 	public void sendToAnalysis() {
 
 		System.out.println("in analysis");
     	/*
 		getResponseFromDB();
 		
 		if(this.response==null){
 
 		}
 		else{
 			
 //			this.subResponse=response.getSubRequestsNotProcessed();
 //			return "response";
 		}
 
 */
 		UUID generatedId = generateId();
 		this.id = generatedId;
 		/*
 		 * store generated id as attribute of requestController
 		 */
 		
 		/*
 		 * create request
 		 */
 		SentimentRequest request = new SentimentRequest();
 		request.setId(generatedId.toString());
 		request.setCompanyName(this.companyName);
 		request.setFrom(this.from);
 		request.setTo(this.to);
 		request.setState(REQUEST_QUEUE_STATE.NEW);
 
 		requestService = new RequestService();
 		requestService.sendRequestToAnalysis(request); 
 		
 	}
 
 	public void getResponseFromDB(){
 	
 		System.out.println("ID: " + this.id);
 		requestService = new RequestService();
 		SentimentRequest requestResponse = requestService.getRequestResponseFromDB(this.id.toString());
 //		/*
 //		 * TEST
 //		 */
 //		SentimentRequest requestResponse = mockSentimentResponse();
 //		/*
 //		 * END TEST
 //		 */
 		calculateResponse(requestResponse);
 		
 	}
 
 	
 	private void calculateResponse(SentimentRequest response) {
 
 		this.response=response;
 		
 		double sumSentiment = 0;
 		long finalNumberOfTweets = 0;
 
 		/*
 		 * calculating request details
 		 */
 
 		for (SentimentProcessingRequest subrequest : response.getSubRequestsProcessed()) {
 
 			long numberOfTweets = subrequest.getNumberOfTweets();
 			if(numberOfTweets > 0){
 				sumSentiment += (subrequest.getSentiment()*numberOfTweets);
 				finalNumberOfTweets += numberOfTweets;
 			}
 
 		}
 
 		double finalSentiment = sumSentiment / finalNumberOfTweets;
 
 		double standardError = 1.96 * Math.sqrt(finalSentiment
 				* (1 - finalSentiment) / (finalNumberOfTweets - 1));
 
 		this.sentiment = finalSentiment;
 		
 		this.numberOfTweets = finalNumberOfTweets;
 		
 		this.minimumSentiment = finalSentiment - standardError;
 		this.maximumSentiment = finalSentiment + standardError;
 
 		System.out.println("Amount: " + numberOfTweets + " - Sentiment: ("
 				+ minimumSentiment + " < " + finalSentiment + " < "
 				+ maximumSentiment + ")");
 	}
 
 	
 
 	@SuppressWarnings("unused")
 	private SentimentRequest mockSentimentResponse(){
 		
 		SentimentProcessingRequest req = new SentimentProcessingRequest();
 		req.setNumberOfTweets(657000);
 		req.setSentiment(0.13f);
 		req.setFrom(new Date());
 		req.setTo(new Date());
 
 		SentimentProcessingRequest pr1 = new SentimentProcessingRequest();
 		pr1.setNumberOfTweets(12);
 		pr1.setSentiment(0.56f);
 		pr1.setFrom(new Date());
 		pr1.setTo(new Date());
 
 		SentimentProcessingRequest pr2 = new SentimentProcessingRequest();
 		pr2.setNumberOfTweets(15);
 		pr2.setSentiment(0.9f);
 		pr2.setFrom(new Date());
 		pr2.setTo(new Date());
 
 		SentimentProcessingRequest pr3 = new SentimentProcessingRequest();
 		pr3.setNumberOfTweets(9751);
 		pr3.setSentiment(0.13f);
 		pr3.setFrom(new Date());
 		pr3.setTo(new Date());
 
 		List<SentimentProcessingRequest> subs = new ArrayList<SentimentProcessingRequest>();
 		subs.add(req);
 		subs.add(pr3);
 		subs.add(pr2);
 		subs.add(pr1);
 
 		SentimentRequest response = new SentimentRequest();
 		response.setFrom(this.from);
 		response.setTo(this.to);
 		response.setCompanyName(this.companyName);
 		
 		return response;
 	}
 
 	public UUID getId() {
 		return id;
 	}
 
 	public long getNumberOfTweets() {
 		return numberOfTweets;
 	}
 
 	public void setNumberOfTweets(long numberOfTweets) {
 		this.numberOfTweets = numberOfTweets;
 	}
 
 	public double getMinimumSentiment() {
 		return minimumSentiment;
 	}
 
 	public void setMinimumSentiment(double minimumSentiment) {
 		this.minimumSentiment = minimumSentiment;
 	}
 
 	public double getMaximumSentiment() {
 		return maximumSentiment;
 	}
 
 	public void setMaximumSentiment(double maximumSentiment) {
 		this.maximumSentiment = maximumSentiment;
 	}
 
 	public RequestService getRequestService() {
 		return requestService;
 	}
 
 	public void setRequestService(RequestService requestService) {
 		this.requestService = requestService;
 	}
 
 	public void setId(UUID id) {
 		this.id = id;
 	}
 
 	public String getCompanyName() {
 		return companyName;
 	}
 
 	public void setCompanyName(String companyName) {
 		this.companyName = companyName;
 	}
 
 	public Date getFrom() {
 		return from;
 	}
 
 	public void setFrom(Date from) {
 		this.from = from;
 	}
 
 	public Date getTo() {
 		return to;
 	}
 
 	public void setTo(Date to) {
 		this.to = to;
 	}
 
 	public double getSentiment() {
 		return sentiment;
 	}
 
 	public void setSentiment(double sentiment) {
 		this.sentiment = sentiment;
 	}
 
 	public SentimentRequest getResponse() {
 		return response;
 	}
 
 	public void setResponse(SentimentRequest response) {
 		this.response = response;
 	}
 
 
 }
 

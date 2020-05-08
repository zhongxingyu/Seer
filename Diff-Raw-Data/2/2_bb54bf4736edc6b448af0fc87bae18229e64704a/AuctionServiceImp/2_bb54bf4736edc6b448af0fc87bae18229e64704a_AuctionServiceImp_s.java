 package com.epam.lab.buyit.controller.service.auction;
 
 import java.util.List;
 
 import com.epam.lab.buyit.controller.dao.auction.AuctionDAO;
 import com.epam.lab.buyit.controller.exception.AuctionAllreadyClosedException;
 import com.epam.lab.buyit.controller.exception.BidAmountException;
 import com.epam.lab.buyit.controller.exception.WrongProductCountException;
 import com.epam.lab.buyit.model.Auction;
 
 public class AuctionServiceImp implements AuctionService {
 	private AuctionDAO auctionDAO;
 
 	public AuctionServiceImp() {
 		auctionDAO = new AuctionDAO();
 	}
 
 	@Override
 	public Auction getItemById(int id) {
 		return auctionDAO.getElementById(id);
 	}
 
 	@Override
 	public List<Auction> getAllItems() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Auction createItem(Auction item) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Auction updateItem(Auction item) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Auction getByProductId(int id) {
 		return auctionDAO.getByProductId(id);
 	}
 
 	@Override
 	public List<Auction> getLatestAuctions(int number) {
 		return auctionDAO.getLatestAuctions(number);
 	}
 
 	@Override
 	public List<Auction> getSoonEndingAuctions(long currentTime, long endTime) {
 		return auctionDAO.getSoonEndingAuctions(currentTime, endTime);
 	}
 
 	@Override
 	public void closeAuction(int auctionId) {
 		auctionDAO.closeAuction(auctionId);
 
 	}
 
 	@Override
 	public boolean buyItServe(int id, int count)
 			throws AuctionAllreadyClosedException, WrongProductCountException {
 		
 		Auction auction = getByProductId(id);
 		String oldStatus = auction.getStatus();
 		if (oldStatus.equals("closed"))
 			throw new AuctionAllreadyClosedException(
 					"Try to bought allready closed auction with id = " + auction.getIdAuction());
 		
 		int oldCount = auction.getCount();
 		if (oldCount < count || count <= 0)
 			throw new WrongProductCountException("Try to bought product with count "
 					+ oldCount + " < requested count" + count);
 
 		String newStatus = "InProgress";
 		if(oldCount == count) newStatus = "closed";
 		
		int result = auctionDAO.buyItServe(auction.getIdAuction(), count, newStatus, oldCount, oldStatus);
 		if (result == 1) {
 			return true;
 		} else
 			return false;
 
 	}
 
 	@Override
 	public int placeBidServe(int idProduct, double bidAmount) throws AuctionAllreadyClosedException, BidAmountException {
 		Auction auction = auctionDAO.getByProductId(idProduct);
 		
 		double currentPrice = auction.getCurrentPrice(); 
 		if(bidAmount <= currentPrice) throw new BidAmountException("Bid amount is to small");
 		
 		String status = auction.getStatus();
 		if(status.equalsIgnoreCase("closed")) 
 			throw new AuctionAllreadyClosedException("Try to palce a bid on allready closed auction with id = " + auction.getIdAuction());
 		
 		int result = auctionDAO.bidServe(auction.getIdAuction(), bidAmount, currentPrice, status);
 		if(result == 1) return auction.getIdAuction();
 		else return 0;
 	}
 }

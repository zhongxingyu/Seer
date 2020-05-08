 package org.jdominion.gui;
 
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jdominion.Card;
 import org.jdominion.CardPile;
 import org.jdominion.Supply;
 
 public class SupplyController implements MouseListener, ActionListener {
 
 	private IGuiInformationSource guiInformationSource;
 	private SupplyView view;
 	private Class<? extends Card> choosenCardType;
 	private Boolean cancelClicked = false;
 	private List<CardImage> currentlyDisplayedCardImages;
 
 	public SupplyView getView() {
 		return view;
 	}
 
 	public SupplyController(IGuiInformationSource guiInformationSource) {
 		this.guiInformationSource = guiInformationSource;
 		this.view = new SupplyView();
 		this.update();
 	}
 
 	public void update() {
 		update(guiInformationSource.getSupply());
 	}
 
 	public void update(Supply availableCards) {
 		List<CardImage> cardImages = new ArrayList<CardImage>();
 
 		Supply completeSupply;
 
 		if (availableCards.isSubsetOf(guiInformationSource.getSupply())) {
 			completeSupply = guiInformationSource.getSupply();
 		} else {
 			completeSupply = availableCards;
 		}
 
 		for (CardPile pile : completeSupply.getCardPiles()) {
 			if (pile.getNumberOfCardsInPile() > 0) {
 				try {
 					CardImage cardImage = new CardImage(pile.getCardClass().newInstance());
 					cardImage.addMouseListener(this);
 					cardImage.setToolTipText(("Available " + pile.getCardName() + ": " + pile.getNumberOfCardsInPile()));
 					cardImage.setGreyedOut(!availableCards.isCardAvailable(pile.getCardClass()));
 					cardImage.setOverlayText(Integer.toString(pile.getNumberOfCardsInPile()));
 					cardImage.setDisplayCost(true);
 					cardImages.add(cardImage);
 				} catch (InstantiationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 
 		}
 		if (cardImagesChange(cardImages)) {
 			this.getView().update(cardImages);
 			this.currentlyDisplayedCardImages = cardImages;
 		}
 	}
 
 	private boolean cardImagesChange(List<CardImage> newCardImages) {
 		if (newCardImages == null && currentlyDisplayedCardImages == null) {
 			return false;
 		}
 		if (newCardImages == null || currentlyDisplayedCardImages == null) {
 			return true;
 		}
 		if (newCardImages.size() != currentlyDisplayedCardImages.size()) {
 			return true;
 		}
 		return !newCardImages.containsAll(currentlyDisplayedCardImages);
 	}
 
	public synchronized Class<? extends Card> chooseCard(final Supply supply) {
 		try {
 			EventQueue.invokeAndWait(new Runnable() {
 				@Override
 				public void run() {
 					update(supply);
 				}
 			});
 		} catch (InterruptedException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (InvocationTargetException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		this.choosenCardType = null;
 		this.cancelClicked = false;
 		while ((this.choosenCardType == null) && (!cancelClicked)) {
 			try {
 				this.wait();
 			} catch (InterruptedException e) {
 
 			}
 		}
		return this.choosenCardType;
 	}
 
 	@Override
 	public synchronized void mouseClicked(MouseEvent e) {
 		if (e.getButton() == MouseEvent.BUTTON1) {
 			this.choosenCardType = ((CardImage) e.getComponent()).getCard().getClass();
 			this.notifyAll();
 		}
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public synchronized void actionPerformed(ActionEvent e) {
 		this.cancelClicked = true;
 		this.notifyAll();
 	}
 
 }

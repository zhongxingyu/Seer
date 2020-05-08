 /*
  * Copyright © 2011, Simon Wrafter <simon.wrafter@gmail.com>
  * 
  * Permission to use, copy, modify, and/or distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package userInterface;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.IOException;
 import java.util.Collection;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import stock.Investments;
 import stock.Market;
 import stock.Portfolio;
 
 public class PortfolioView extends JFrame implements WindowListener {
 	private static final long serialVersionUID = -7129233116646387153L;
 	private MainCommandPanel mainCommandPanel;
 	private MainMenuBar mainMenuBar;
 	private MainPanel mainPanel;
 	private Investments investments;
 	private Actions	currentView;
 
 	public PortfolioView()
 			throws IOException, ParserConfigurationException, SAXException {
 		super("OMXInvest");
 		JLabel label = new JLabel("Starting...", JLabel.CENTER);
 		label.setPreferredSize(new Dimension(480, 300));
 		this.add(label);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.pack();
 		this.setLocationRelativeTo(this.getOwner());
 		this.setVisible(true);
 
 		investments = new Investments(label);
 
 		this.setVisible(false);
 		this.remove(label);
 
 		mainCommandPanel = new MainCommandPanel(this);
 		this.add(mainCommandPanel, BorderLayout.SOUTH);
 
 		mainPanel = new MainPanel(this);
 		this.add(mainPanel, BorderLayout.CENTER);
 		currentView = Actions.HOME;
 
 		mainMenuBar = new MainMenuBar(this);
 		this.setJMenuBar(mainMenuBar);
 
 		this.addWindowListener(this);
 		this.pack();
 		this.setLocationRelativeTo(this.getOwner());
 		this.setVisible(true);
 		System.out.println("Done!");
 	}
 
 	public Collection<Market> getMarketSet() {
 		return investments.getMarketSet();
 	}
 
 	public Collection<Portfolio> getPortfolios() {
 		return investments.getPortfolios();
 	}
 
 	public Portfolio getCurrentPortfolio() {
 		return investments.getCurrentPortfolio();
 	}
 
 	public Double[][] getPortfolioHistory() {
 		return investments.getHistory(4);
 	}
 
 	public Double[][] getPortfolioHistory(int nbrOfDays) {
 		return investments.getHistory(4, nbrOfDays);
 	}
 
 	public void updateOptimization() {
 		mainPanel.updateOptimization();
 	}
 
 	public double portfolioValue() {
 		return investments.getPortfolioValueSum();
 	}
 
 	public Double[] getPortfolioDistribution() {
 		return investments.getPortfolioDistribution();
 	}
 
 	public double getPortfolioLiquid() {
 		return investments.getPortfolioLiquid();
 	}
 
 	public void setPortfolioLiquid(double value) {
 		investments.setPortfolioLiquid(value);
 	}
 
 	public String[] getStockIds() {
 		return investments.getStockIds();
 	}
 
 	public String[] getStockNames() {
 		return investments.getStockNames();
 	}
 
 	public String[] getShortNames() {
 		return investments.getShortNames();
 	}
 
 	public void actionHandler(Actions actions) {
 		actionHandler(actions, null);
 	}
 
 	public void actionHandler(Actions actions, String additionalInfo) {
 		switch (actions) {
 		case HOME:
 			mainPanel.showHome();
 			currentView = Actions.HOME;
 			break;
 		case HISTORY:
 			mainPanel.showHistory();
 			currentView = Actions.HISTORY;
 			break;
 		case OPTIMAL:
 			mainPanel.showOptimization();
 			currentView = Actions.OPTIMAL;
 			break;
 		case MARKET:
 			mainPanel.showMarkets();
 			currentView = Actions.MARKET;
 			break;
 		case ADD:
 			String omxId_add = MainOptionPane.getString("Type omxId of stock to add to portfolio:");
 			if (omxId_add == null) { break; }
 			omxId_add = omxId_add.toUpperCase();
 			try {
 				investments.addStockToPortfolio(omxId_add);
 			} catch (Exception e) {
 				MainOptionPane.errorPopUp(e.getMessage());
 			}
 			mainPanel.showHome();
 			currentView = Actions.HOME;
 			break;
 		case REMOVE:
 			String omxId_remove = MainOptionPane.getString("Type omxId of stock to remove from portfolio:");
 			if (omxId_remove == null) { break; }
 			omxId_remove = omxId_remove.toUpperCase();
 			if (!investments.removeStockfromPortfolio(omxId_remove)) {
 				MainOptionPane.errorPopUp("No company with id " + omxId_remove + " was found in this portfolio");
 				break;
 			}
 			mainPanel.showHome();
 			currentView = Actions.HOME;
 			break;
 		case BUY:
 			String omxId_buy = MainOptionPane.getString("Type omxId of stock to buy shares in:");
 			if (omxId_buy == null) { break; }
 			omxId_buy = omxId_buy.toUpperCase();
 			if (!getCurrentPortfolio().contains(omxId_buy)) {
 				MainOptionPane.errorPopUp("No company with id " + omxId_buy + " was found in this portfolio");
 				break;
 			}
 			getCurrentPortfolio().buy(omxId_buy,
 					MainOptionPane.getInteger("How many shares do you wish to buy?"),
 					investments.getLastValue(omxId_buy));
 			mainPanel.showHome();
 			currentView = Actions.HOME;
 			break;
 		case SELL:
 			String omxId_sell = MainOptionPane.getString("Type omxId of stock to sell shares of:");
 			if (omxId_sell == null) { break; }
 			omxId_sell = omxId_sell.toUpperCase();
 			if (!getCurrentPortfolio().contains(omxId_sell)) {
 				MainOptionPane.errorPopUp("No company with id " + omxId_sell + " was found in this portfolio");
 				break;
 			}
 			getCurrentPortfolio().sell(omxId_sell,
 					MainOptionPane.getInteger("How many shares do you wish to sell?"),
 					investments.getLastValue(omxId_sell));
 			mainPanel.showHome();
 			currentView = Actions.HOME;
 			break;
 		case LIQUID:
 			Double asset = MainOptionPane.getDouble("Set liquid asset to:");
 			if (asset == null) { break; }
 			getCurrentPortfolio().setLiquidAsset(asset);
 			mainPanel.showHome();
 			currentView = Actions.HOME;
 			break;
 		case NEW:
 			Portfolio portfolio = MainOptionPane.makeNewPortfolio();
 			if (portfolio == null) { break; }
 			if (!investments.addNewPortfolio(portfolio)) {
 				MainOptionPane.errorPopUp("A portfolio with this name already exists");
 				break;
 			}
 			mainMenuBar.addPortfolio(portfolio);
 			actionHandler(Actions.SWITCH, portfolio.getName());
 			break;
 		case DELETE:
 			String name = MainOptionPane.getString("Name of portfolio to remove");
 			if (name == null) { break; }
 			if (investments.getPortfolios().size() < 2) {
 				MainOptionPane.errorPopUp("You can not remove any more portfolios");
 				break;
 			}
 			if (!investments.removePortfolio(name)) {
 				MainOptionPane.errorPopUp("No portfolio with this name exists");
 				break;
 			}
 			mainMenuBar.removePortfolio(name);
 			actionHandler(Actions.SWITCH);
 			break;
 		case EDIT:
 			String oldName = getCurrentPortfolio().getName();
 			MainOptionPane.editPortfolioName(getCurrentPortfolio());
 			mainMenuBar.removePortfolio(oldName);
 			mainMenuBar.addPortfolio(getCurrentPortfolio());
 			break;
 		case SWITCH:
 			investments.setCurrentPortfolio(additionalInfo);
 			actionHandler(currentView);
 			break;
 		case REBUILD_HISTORY:
 			try {
 				investments.rebuildHistory();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			actionHandler(currentView);
 			break;
 		case UPDATE_MARKETS:
 			investments.updateMarkets();
 			break;
 		case ABOUT:
			MainOptionPane.infoPopUp(
					"This program, OMXInvest, is being developed by me, Simon Wrafter,\n" +
					"because it is good fun. Also it might come in handy, if not for me,\n" +
					"maybe for you or your friend.\n\n" +
					"If you find it to be useful or just like java programming or any\n" +
 					"other valid reason, feel free to participate in what way you can.\n" +
 					"Or just send me an e mail with a happy smiley.\n\n" +
					"OMXInvest is mainley licensed under the ICS license, which can be\n" +
					"found under 'ICS License' in the 'More' menu. Some of the code is\n" +
					"public domain, a copy right notice can be found under 'JAMA © Notice'" +
					"in the 'More' menu.\n\n" +
 					"Code hosted at https://github.com/simonwrafter/OMXInvest");
 			break;
 		case ICS_LICENSE:
 			MainOptionPane.infoPopUp("Copyright (c) 2011, Simon Wrafter <simon.wrafter@gmail.com>\n\n" +
 					"Permission to use, copy, modify, and/or distribute this software for any\n" +
 					"purpose with or without fee is hereby granted, provided that the above\n" +
 					"copyright notice and this permission notice appear in all copies.\n\n" +
 					"THE SOFTWARE IS PROVIDED \"AS IS\" AND THE AUTHOR DISCLAIMS ALL WARRANTIES\n" +
 					"WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF\n" +
 					"MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR\n" +
 					"ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES\n" +
 					"WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN\n" +
 					"ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF\n" +
 					"OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.");
 			break;
 		case JAMA_C:
 			MainOptionPane.infoPopUp("JAMA Copyright Notice\n\n" +
 					"This software is a cooperative product of The MathWorks and the National\n" +
 					"Institute of Standards and Technology (NIST) which has been released to the\n" +
 					"public domain. Neither The MathWorks nor NIST assumes any responsibility\n" +
 					"whatsoever for its use by other parties, and makes no guarantees, expressed\n" +
 					"or implied, about its quality, reliability, or any other characteristic.\n\n" +
 					"Read more at http://math.nist.gov/javanumerics/jama/.");
 			break;
 		}
 	}
 
 	@Override
 	public void windowOpened(WindowEvent e) {
 		// Auto-generated method stub
 	}
 
 	@Override
 	public void windowClosing(WindowEvent e) {
 		this.setVisible(false);
 		investments.save();
 	}
 
 	@Override
 	public void windowClosed(WindowEvent e) {
 		// Auto-generated method stub
 	}
 
 	@Override
 	public void windowIconified(WindowEvent e) {
 		// Auto-generated method stub
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent e) {
 		// Auto-generated method stub
 	}
 
 	@Override
 	public void windowActivated(WindowEvent e) {
 		// Auto-generated method stub
 	}
 
 	@Override
 	public void windowDeactivated(WindowEvent e) {
 		// Auto-generated method stub
 	}
 }

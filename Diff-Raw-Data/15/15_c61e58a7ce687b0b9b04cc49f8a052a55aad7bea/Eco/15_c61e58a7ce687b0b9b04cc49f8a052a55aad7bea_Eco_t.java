 package nl.giantit.minecraft.GiantShop.core.Eco;
 
 import java.util.logging.Level;
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.core.config;
 import nl.giantit.minecraft.GiantShop.core.Eco.Engines.*;
 
 /**
  *
  * @author Giant
  */
 public class Eco {
 	
 	private iEco Engine = null;
 	private GiantShop plugin;
 	private config conf = config.Obtain();
 	Engines engine;
 	
 	public enum Engines {
 		AECO,
 		CRAFTCONOMY,
 		CURRENCYCORE,
 		ECONXP,
 		ESSENTIALS,
 		MCMONEY,
 		MINECONOMY,
 		MULTICURRENCY,
 		BOSE6,
		BOSE7,
 		EWALLET,
 		IC3O,
 		ICONOMY4,
 		ICONOMY5,
 		ICONOMY6
 	}
 	
 	private boolean packageExists(String...Packages) {
 		try{
 			for(String pckg : Packages) {
 				Class.forName(pckg);
 			}
 			return true;
 		}catch(ClassNotFoundException e) {
 			return false;
 		}
 	}
 	
 	private Engines findEngine(String engine) {
 		if(engine.equalsIgnoreCase("AECO")) {
 			return Engines.AECO;
 		}else if(engine.equalsIgnoreCase("CRAFTCONOMY")) {
 			return Engines.CRAFTCONOMY;
 		}else if(engine.equalsIgnoreCase("CURRENCYCORE")) {
 			return Engines.CURRENCYCORE;
 		}else if(engine.equalsIgnoreCase("ECONXP")) {
 			return Engines.ECONXP;
 		}else if(engine.equalsIgnoreCase("ESSENTIALS")) {
 			return Engines.ESSENTIALS;
 		}else if(engine.equalsIgnoreCase("MCMONEY")) {
 			return Engines.MCMONEY;
 		}else if(engine.equalsIgnoreCase("MINECONOMY")) {
 			return Engines.MINECONOMY;
 		}else if(engine.equalsIgnoreCase("MULTICURRENCY")) {
 			return Engines.MULTICURRENCY;
 		}else if(engine.equalsIgnoreCase("BOSE6")) {
 			return Engines.BOSE6;
		}else if(engine.equalsIgnoreCase("BOSE7")) {
			return Engines.BOSE7;
 		}else if(engine.equalsIgnoreCase("EWALLET")) {
 			return Engines.EWALLET;
 		}else if(engine.equalsIgnoreCase("IC3O")) {
 			return Engines.IC3O;
 		}else if(engine.equalsIgnoreCase("ICONOMY4")) {
 			return Engines.ICONOMY4;
 		}else if(engine.equalsIgnoreCase("ICONOMY5")) {
 			return Engines.ICONOMY5;
 		}else if(engine.equalsIgnoreCase("ICONOMY6")) {
 			return Engines.ICONOMY6;
 		}
 		
 		return null;
 	}
 	
 	public Eco(GiantShop plugin) {
 		this.plugin = plugin;
 		this.engine = this.findEngine(conf.getString("GiantShop.Economy.Engine"));
 		
 		switch(this.engine) {
 			case AECO:
 				if(packageExists("org.neocraft.AEco.AEco")) {
 					//AEco
 					Engine = new AEco_Engine(this.plugin);
 				}
 				break;
 			case CRAFTCONOMY:
 				if(packageExists("me.greatman.Craftconomy.Craftconomy")) {
 					//CraftConomy
 					plugin.getLogger().log(Level.WARNING, "[" + plugin.getName() + "] CraftConomy is currently not yet supported!");
 				}
 				break;
 			case CURRENCYCORE:
 				if(packageExists("is.currency.Currency")) {
 					//CurrencyCore
 					plugin.getLogger().log(Level.WARNING, "[" + plugin.getName() + "] CurrencyCore is currently not yet supported!");
 				}
 				break;
 			case ECONXP:
 				if(packageExists("ca.agnate.EconXP.EconXP")) {
 					//EconXP
 					plugin.getLogger().log(Level.WARNING, "[" + plugin.getName() + "] EconXP is currently not yet supported!");
 				}
 				break;
 			case ESSENTIALS:
 				if(packageExists("com.earth2me.essentials.api.Economy", "com.earth2me.essentials.api.NoLoanPermittedException", "com.earth2me.essentials.api.UserDoesNotExistException")) {
 					//Essentials
 					Engine = new Essentials_Engine(this.plugin);
 				}
 				break;
 			case MCMONEY:
 				if(packageExists("boardinggamer.mcmoney.McMoneyAPI")) {
 					//McMoney
 					plugin.getLogger().log(Level.WARNING, "[" + plugin.getName() + "] McMoney is currently not yet supported!");
 				}
 				break;
 			case MINECONOMY:
 				if(packageExists("me.mjolnir.mineconomy.MineConomy")) {
 					//MineConomy
 					plugin.getLogger().log(Level.WARNING, "[" + plugin.getName() + "] MineConomy is currently not yet supported!");
 				} 
 				break;
 			case MULTICURRENCY:
 				if(packageExists("me.ashtheking.currency.Currency", "me.ashtheking.currency.CurrencyList")) {
 					//MultiCurrency
 					plugin.getLogger().log(Level.WARNING, "[" + plugin.getName() + "] MultiCurrency is currently not yet supported!");
 				}
 				break;
			case BOSE6:
 				if(packageExists("cosine.boseconomy.BOSEconomy", "cosine.boseconomy.CommandManager")) {
 					//BOSE6
 					Engine = new bose6_Engine(this.plugin);
 				}
 				break;
			case BOSE7:
 				if(packageExists("cosine.boseconomy.BOSEconomy", "cosine.boseconomy.CommandHandler")) {
 					//BOSE7
 					Engine = new bose7_Engine(this.plugin);
 				}
 				break;
 			case EWALLET:
 				if(packageExists("me.ethan.eWallet.ECO")) {
 					//eWallet
 					plugin.getLogger().log(Level.WARNING, "[" + plugin.getName() + "] eWallet is currently not yet supported!");
 				}
 				break;
 			case IC3O:
 				if(packageExists("me.ic3d.eco.ECO")) {
 					//ic3o
 					plugin.getLogger().log(Level.WARNING, "[" + plugin.getName() + "] 3co is currently not yet supported!");
 				}
 				break;
 			case ICONOMY4:
 				if(packageExists("com.nijiko.coelho.iConomy.iConomy", "com.nijiko.coelho.iConomy.system.Account")) {
 					//ic4
 					Engine = new ic4_Engine(this.plugin);
 				}
 				break;
 			case ICONOMY5:
 				if(packageExists("com.iConomy.iConomy", "com.iConomy.system.Account", "com.iConomy.system.Holdings")) {
 					//ic5
 					Engine = new ic5_Engine(this.plugin);
 				}
 				break;
 			case ICONOMY6:
 				if(packageExists("com.iCo6.iConomy")) {
 					//ic6
 					Engine = new ic6_Engine(this.plugin);
 				}
 				break;
 		}
 	}
 	
 	public boolean isLoaded() {
 		return (this.Engine != null && this.Engine.isLoaded());
 	}
 	
 	public iEco getEngine() {
 		return this.Engine;
 	}
 }

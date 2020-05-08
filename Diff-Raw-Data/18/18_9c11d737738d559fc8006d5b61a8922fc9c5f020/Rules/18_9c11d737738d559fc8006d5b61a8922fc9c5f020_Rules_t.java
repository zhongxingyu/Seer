 package com.github.mineGeek.ItemRules.Rules;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 
 import com.github.mineGeek.ItemRestrictions.Utilities.Config;
 import com.github.mineGeek.ItemRules.Integration.FactionsPlayer;
 import com.github.mineGeek.ItemRules.Integration.McMMOPlayer;
 import com.github.mineGeek.ItemRules.Rules.Rule.RuleMode;
 import com.github.mineGeek.ItemRules.Store.IRPlayer;
 import com.github.mineGeek.ItemRules.Store.Players;
 
 /**
  * Static object for interacting with rules
  *
  */
 public class Rules {
 
 
 	/**
 	 * Stores aliases between tags and lists of item id's. eg. 'woodenstuff' => 268, 269
 	 */
 	public static Map<String, List<String>> itemAliases = new HashMap<String, List<String>>();
 	
 	
 	/**
 	 * ArrayList of all rules
 	 */
 	private static List<Rule> ruleList = new ArrayList<Rule>();
 	
 	
 	/**
 	 * main Map of rules accessible via their tag
 	 * Key = rule.tag/ Value = rule
 	 */
 	private static Map<String, Rule> ruleByTagList = new HashMap<String, Rule>();
 	
 	
 	public static List<String> getPlayerRulesList( Player player, boolean doCan, boolean doRestricted, boolean doUnapplied ) {
 		
 		List<String> result = new ArrayList<String>();
 		
 		//Get current rules for player
 		Map< String, List<String>> rules = Players.get( player ).getRuleMatrix();
 		
 		if ( !rules.isEmpty() ) {
 			
 			
 			for ( String key : rules.keySet() ) {
 				
 				if ( key.equals( "unrestricted" ) && !doCan ) continue;
 				if ( key.equals( "restricted" ) && !doRestricted ) continue;
 				if ( key.equals( "unapplied" ) && !doUnapplied ) continue;
 				
 				String s = null;
 				
 				for ( String x : rules.get(key) ) {
 					
 					if ( Rules.getRule( x ).getDescription() != null ) {
 						if ( s != null ) {
 							s = s + ", " + Rules.getRule( x ).getDescription();
 						} else {
 							s = Rules.getRule( x ).getDescription();
 						}				
 					}
 				}
 				
 				if ( s != null ) {
 					result.add( ( key == "unrestricted" ? ChatColor.GREEN + Config.txtCanDoPrefix : key == "restricted" ? ChatColor.RED + Config.txtCannotDoPrefix : ChatColor.YELLOW )  + s );
 				}			
 				
 			}
 		
 		}
 		
 		return result;
 		
 	}
 
 	/**
 	 * Returns a list of all rules that apply to a player
 	 * @param player
 	 * @param XPLevel
 	 * @param itemLevel
 	 * @return
 	 */
 	public static Map<String, RuleData> getPlayerRules( Player player ) {
 		
 		
 		Map<String, RuleData> rules = new HashMap<String, RuleData>();
 
 		RuleMode mode = RuleMode.DEFAULT;
 		IRPlayer ps = Players.get(player.getName());
 		ps.clearRuleMatrix( true );
 		
 		if ( !ruleList.isEmpty() ) {
 			
 			
 			ps.setRuleMode( RuleMode.DEFAULT );
 			/**
 			 * Automatically apply rules
 			 */
 			
 			for ( Rule x : Rules.ruleList ) {
 		
 				if ( x.getAuto() ) {
 					
 					if ( x.appliesToPlayer( ps ) ) {
 						
 						if ( x.getRuleMode() != RuleMode.DEFAULT ) {
 							mode = x.getRuleMode();
 							if ( mode == RuleMode.ALLOWPREVIOUS || mode == RuleMode.DENYPREVIOUS ) {
 								if ( !rules.isEmpty() ) {
 									ps.clearRuleMatrix( false );
 									rules.clear();
 								}
 								
 								ps.setRuleMode( mode == RuleMode.ALLOWPREVIOUS ? RuleMode.ALLOW : RuleMode.DENY );
 							}
 						}
 						
 						ps.addRuleMatrixItem( ( mode == RuleMode.ALLOW || mode == RuleMode.ALLOWPREVIOUS ? "allowed" : "restricted" ), x.getTag() );
 						
 						Map<String, RuleData> r = x.getAllowedItems();
 						
 						if ( !r.isEmpty() ) {
 							
 							for( String y : r.keySet() ) {
 								rules.put(y, new RuleData( r.get(y) ) );
 							}
 							
 						}
 						
 						r = x.getRestrictedItems();
 						
 						if ( !r.isEmpty() ) {
 							
 							for ( String y : r.keySet() ) {
 								rules.put(y, new RuleData( r.get(y) ) );
 							}
 							
 						}
 						
 					} else {
 						ps.addRuleMatrixItem( "unapplied", x.getTag() );
 					}
 					
 				}
 			}
 			
 			/**
 			 * Apply manual rule (may override existing rules)
 			 */
 			if ( !ps.getManualRules().isEmpty() ) {
 			
 				for ( String x : ps.getManualRules() ) {
 					
 					if ( Rules.ruleByTagList.containsKey( x ) ) {
 					
 						if ( Rules.ruleByTagList.get(x).appliesToPlayer( ps ) ) {
 						
 							Map<String, RuleData> r = Rules.ruleByTagList.get(x).getAllowedItems();							
							r.putAll( Rules.ruleByTagList.get(x).getRestrictedItems() );
 							if ( Rules.ruleByTagList.get(x).getRuleMode() != RuleMode.DEFAULT ) {
 								mode = Rules.ruleByTagList.get(x).getRuleMode();
 								
 								if ( mode == RuleMode.ALLOWPREVIOUS || mode == RuleMode.DENYPREVIOUS ) {
 									
 									if ( !rules.isEmpty() ) {
 										ps.clearRuleMatrix( false );
 										rules.clear();
									} 
 									
 									ps.setRuleMode( mode == RuleMode.ALLOWPREVIOUS ? RuleMode.ALLOW : RuleMode.DENY );
 								}								
 							}
 							
 							ps.addRuleMatrixItem( ( mode == RuleMode.ALLOW || mode == RuleMode.ALLOWPREVIOUS ? "allowed" : "restricted" ), x );
 							
 							if ( !r.isEmpty() ) {
 								
 								for ( String y : r.keySet() ) {
 									rules.put(y, new RuleData( r.get(y) ) );
 								}
 								
 							}
 							
 						} else {
 							ps.addRuleMatrixItem( "unapplied", x );
 						}
 						
 					}
 					
 				}
 				
 				
 			}
 		}
 		//Players.get(player).setRuleMode(mode);
 		return rules;
 
 	}
 	
 
 	/**
 	 * Return a rule by its tag name
 	 * @param tagName
 	 * @return
 	 */
 	public static Rule getRule( String tagName ) {
 		return Rules.ruleByTagList.get( tagName );
 	}
 	
 	
 	/**
 	 * Removes a rule from the list. Will no longer be applied to players
 	 * @param rule
 	 */
 	public static void removeRule( Rule rule ) {
 		
 		rule.close();
 		Rules.ruleByTagList.remove( rule.getTag() );
 		Rules.ruleList.remove( rule );
 		
 	}
 	
 	
 	/**
 	 * Adds a rule to the Rules list
 	 * @param rule
 	 */
 	public static void addRule( Rule rule ) {
 		
 		Rules.ruleByTagList.put( rule.getTag(), rule );
 		Rules.ruleList.add( rule );		
 	}
 	
 
 	public static void addRule( String tag, ConfigurationSection config ) {
 		
 		Rules.addRule( tag, config, false );
 		
 	}
 	
 	
 	/**
 	 * Build, then add a rule from the config file
 	 * @param tag
 	 * @param config
 	 */
 	public static void addRule( String tag, ConfigurationSection config, boolean manual ) {
 		
 		Rule rule = null;
 
 		if ( config.isSet( "inherit") ) {
 			
 			rule = Rules.ruleByTagList.get( config.getString( "inherit"));
 			
 			if ( rule != null ) {
 				
 				rule = new Rule( tag, rule );
 				
 			}
 			
 		}
 		
 		if ( rule == null ) {
 			
 			 rule = new Rule( tag );
 			
 		}
 		
 		if ( McMMOPlayer.enabled ) {
 		
 			if ( config.isSet( "mcmmo" ) ) {
 				
 				ConditionMcMMO mcMMO = new ConditionMcMMO( config.getBoolean( "mcmmo.party", false) );
 				
 				if ( config.isSet( "mcmmo.skills" )) {
 					for ( String x : config.getConfigurationSection( "mcmmo.skills" ).getKeys( false ) ) {
 						mcMMO.add( x.toUpperCase(), config.getInt( "mcmmo.skills." + x, 0) );
 					}
 				}
 			
 				rule.addApplicator( mcMMO );
 			}
 			
 		}
 		
 		if ( FactionsPlayer.enabled ) {
 			
 			if ( config.isSet( "factions" ) ) {
 				
 				ConditionFactions factions = new ConditionFactions();
 				
 				if ( config.isSet( "factions.appliesTo" ) ) {
 					factions.setWhitelist( config.getStringList( "factions.appliesTo" ) );
 				}
 				
 				if ( config.isSet( "factions.exclude" ) ) {
 					factions.setBlacklist( config.getStringList( "factions.exclude" ) );
 				}
 				
 				if ( config.isSet( "factions.maxPower" ) || config.isSet( "factions.minPower" )) {
 					factions.setPower( config.getInt( "factions.minPower" ), config.getInt( "factions.maxPower") );
 				}
 				
 				if ( factions != null ) rule.addApplicator( factions );
 			}
 			
 		}
 		
 		
 		if ( config.isSet(  "mode" ) ) {
 			
 			rule.setRuleMode( RuleMode.valueOf( config.getString( "mode" ).toUpperCase()) );
 			
 		}
 		
 		rule.setAuto( config.getBoolean( "auto", !manual ) );
 		
 		if ( config.isSet( "description") ) rule.setDescription( config.getString( "description" ) );
 		
 		if ( config.isSet( "messages.restricted") ) rule.setRestrictedMessage( config.getString( "messages.restricted", null) );
 		if ( config.isSet( "messages.unrestricted") ) rule.setUnrestrictedMessage( config.getString( "messages.unrestricted", null) );
 		
 		if ( config.isSet( "actions") ) 	rule.setActions( config.getStringList( "actions" ) );
 		//if ( config.isSet("items") ) 	rule.setAllowedItems( config.getStringList("items") );
 		
 		if ( config.isSet( "items.allow" ) ) Rules.loadItemsFromList( config.getStringList( "items.allow" ), rule, true );
 		if ( config.isSet( "items.restrict" ) ) Rules.loadItemsFromList( config.getStringList( "items.restrict" ), rule, false );
 		
 		if ( config.isSet( "itemsAdd" ) ) {
 			
 			for( String x : config.getStringList( "itemsAdd" ) ) {
 				rule.addAllowedItem( x );
 			}
 			
 		}
 		
 		if ( config.isSet( "itemsRemove" ) ) {
 			
 			for( String x : config.getStringList( "itemsRemove" ) ) {
 				rule.removeAllowedItem( x );
 			}
 			
 		}
 		
 		if ( config.isSet( "permissions.applyto" ) ) rule.addApplicator( new ConditionPerms( true, config.getStringList(  "permissions.applyto" ) ) );
 		if ( config.isSet( "permissions.exclude" ) ) rule.addApplicator( new ConditionPerms( false, config.getStringList(  "permissions.exclude" ) ) );		
 			
 		if ( config.isSet( "groups.applyto" ) ) rule.addApplicator( new ConditionGroup( true, config.getStringList(  "groups.applyto" ) ) );
 		if ( config.isSet( "groups.exclude" ) ) rule.addApplicator( new ConditionGroup( false, config.getStringList(  "groups.exclude" ) ) );		
 		
 		if ( config.isSet( "worlds.applyto" ) ) rule.addApplicator( new ConditionWorld( true, config.getStringList(  "worlds.applyto" ) ) );
 		if ( config.isSet( "worlds.exclude" ) ) rule.addApplicator( new ConditionWorld( true, config.getStringList(  "worlds.exclude" ) ) );
 		
 		if ( config.isSet( "xp.min" ) || config.isSet(  "xp.max" ) ) {
 			rule.addApplicator( new ConditionXP( config.isSet( "xp.min" ) ? config.getInt( "xp.min" ) : null, config.isSet( "xp.max" ) ? config.getInt( "xp.max" ) : null ) );	
 		}
 
 		if ( config.isSet( "itemLevel.min" ) || config.isSet( "itemLevel.max" ) ) {
 			rule.addApplicator( new ConditionItemLevel( config.isSet( "itemLevel.min" ) ? config.getInt( "itemLevel.min" ) : null, config.isSet( "itemLevel.max" ) ? config.getInt( "itemLevel.max" ) : null ) );	
 		}
 		
 		Rules.addRule( rule );
 		
 		
 	}
 	
 	/**
 	 * Add an item alias.
 	 * This way you can refer to user made lists of item ids by
 	 * a name instead of the itemids.
 	 * @param tag
 	 * @param items
 	 */
 	public static void addItemAlias( String tag, List<String> items ) {
 		
 		itemAliases.put( tag, items );
 		
 	}
 	
 	
 	/**
 	 * Returns the list of item ids for the tag
 	 * @param tag
 	 * @return
 	 */
 	public static List<String> getItemsFromAlias( String tag ) {
 		
 		return itemAliases.get( tag);
 		
 	}
 	
 	
 	/**
 	 * Loads the items from the alias
 	 * @param list
 	 * @param rule
 	 * @param isAllowedItem
 	 */
 	public static void loadItemsFromList( List<String> list, Rule rule, boolean isAllowedItem ) {
 		
 		if ( !list.isEmpty() ) {
 
 			List<String> aliases;
 			
 			for ( String x : list ) {
 				
 				aliases = getItemsFromAlias( x );
 				
 				if ( aliases == null ) {
 					// no alias. Just add
 					if ( isAllowedItem ) {
 						rule.addAllowedItem( x );
 					} else {
 						rule.addRestrictedItem( x );
 					}
 				} else {
 					if ( isAllowedItem ) {
 						rule.addAllowedItems( aliases );
 					} else {
 						rule.addRestrictedItems( aliases );
 					}
 				}
 				
 			}
 			
 		}
 		
 	}
 	
 	
 	/**
 	 * Returns number of rules loaded
 	 * @return
 	 */
 	public static int count() {
 		return Rules.ruleList.size();
 	}
 	
 	
 	/**
 	 * Good Guy Closure
 	 */
 	public static void close() {
 		
 		if ( ruleList != null && !ruleList.isEmpty() ) {
 			for ( Rule r : ruleList ) {
 				r.close();
 			}
 			ruleList.clear();
 		}
 		
 		if ( itemAliases != null ) itemAliases.clear();
 		if ( ruleByTagList != null ) ruleByTagList.clear();
 		
 	}
 	
 }

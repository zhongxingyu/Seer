 package uk.org.downesward.dirtside;
 
 import uk.org.downesward.dirtside.R;
 import uk.org.downesward.dirtside.domain.CombatResolutionConfig;
 import uk.org.downesward.dirtside.domain.CombatResolutionResult;
 import uk.org.downesward.dirtside.domain.Utilities;
 import uk.org.downesward.dirtside.domain.Weapon;
 import uk.org.downesward.utiliites.Dice;
 
 import android.app.Activity;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.os.Bundle;
 
 public class CombatResolutionActivity extends Activity implements
 		CombatResolutionConfigFragment.ResolveCombat {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.combatresolution);
 
 	}
 
 	@Override
 	public CombatResolutionResult resolveNormalCombat(
 			CombatResolutionConfig config) {
 		Weapon weapon = new Weapon(config.getWeaponType(), "");
 		weapon.setSize(config.getWeaponSize());
 		Integer weaponType = 0;
 		StringBuilder resultOut = new StringBuilder();
 		Resources res = getResources();
 		int atkRoll = 0;
 		int defRoll = 0;
 		CombatResolutionResult result = new CombatResolutionResult();
 
 		if (weapon.getType().equals(Weapon.IAVR)) {
 			weaponType = 1;
 		} else if (weapon.getType().equals(Weapon.APSW)) {
 			weaponType = 2;
 		}
 
 		Integer apparentRange;
 		Integer whichRange = weapon.Range(config.getRange(), this);
 		apparentRange = whichRange;
 		if (apparentRange > 0) {
 			// High signature targets are treated as being closer
 			if (config.getTargetSize() == 6) {
 				apparentRange--;
 			} else if (config.getTargetSize() == 7) {
 				apparentRange -= 2;
 			}
 			if (apparentRange < 1) {
 				apparentRange = 1;
 			}
 		}
 		if (apparentRange > 0) {
 			if (weaponType == 0) {
 				// convert the firecon and target levels to dice
 				int fireconDice = fireControlToDice(weapon.getType(),
 						config.getFireControl(), apparentRange);
 				int targetDice = signatureToDice(config.getTargetSize());
 				if (config.isMoving()) {
 					// The firer moved, drop the dice one level
 					fireconDice -= 2;
 				}
 				if (fireconDice < 4) {
 					// Missed, firecon not capable
 					resultOut.append(res.getString(R.string.msg_not_capable));
 					atkRoll = 0;
 					defRoll = 1;
 				} else {
 					int secondary = 0;
 					// For normal systems roll the firecon and the secondary die
 					// For GMS roll ADS, PDS and ECM
 					if (weapon.getType().equals(Weapon.GUIDED_MISSILE)
 							|| weapon.getType().equals(
 									Weapon.HIGH_VELOCITY_MISSILE)) {
 						if (config.isInfantry()) {
 							resultOut
 									.append(res
 											.getString(R.string.msg_missiles_cant_shoot_inf));
 							atkRoll = 0;
 							defRoll = 1;
 						} else {
 							defRoll = 0;
 							Dice attackDice = new Dice(fireconDice);
 							atkRoll = attackDice.roll();
 
 							Dice defenceDie;
 							Integer secondaryRoll;
 
 							resultOut.append(String.format(
 									res.getString(R.string.msg_attack_result),
 									fireconDice, atkRoll));
 							// HVMS ignore ADS and PDS
 							if (weapon.getType().equals(Weapon.GUIDED_MISSILE)) {
 								secondary = DSToDice(config.getAds());
 								if (secondary > 0) {
 									defenceDie = new Dice(secondary);
 									secondaryRoll = defenceDie.roll();
 									resultOut
 											.append(String.format(
 													res.getString(R.string.msg_ads_result),
 													defenceDie, secondaryRoll));
 									if (secondaryRoll > defRoll) {
 										defRoll = secondaryRoll;
 									}
 								}
 								secondary = DSToDice(config.getPds());
 								if (secondary > 0) {
 									defenceDie = new Dice(secondary);
 									secondaryRoll = defenceDie.roll();
 									resultOut
 											.append(String.format(
 													res.getString(R.string.msg_pds_result),
 													defenceDie, secondaryRoll));
 									if (secondaryRoll > defRoll) {
 										defRoll = secondaryRoll;
 									}
 								}
 							}
 							// ECM always effects missiles
 							secondary = ECMToDice(config.getEcm());
 							if (secondary > 0) {
 								defenceDie = new Dice(secondary);
 								secondaryRoll = defenceDie.roll();
 								resultOut.append(String.format(
 										res.getString(R.string.msg_ecm_result),
 										defenceDie, secondaryRoll));
 								if (secondaryRoll > defRoll) {
 									defRoll = secondaryRoll;
 								}
 							}
 						}
 					} else {
 						secondary = secondaryToDice(config.getTargetState());
 						resultOut.append(String.format(
 								res.getString(R.string.msg_result_die),
 								fireconDice, targetDice, secondary));
 
 						// Now do the to hit rolls
 						Dice attackDice = new Dice(fireconDice);
 						Dice defenceDice = new Dice(targetDice);
 						Dice secondaryDice = new Dice(secondary);
 						atkRoll = attackDice.roll();
 						defRoll = defenceDice.roll();
 						Integer secondaryRoll = secondaryDice.roll();
 						resultOut.append(String.format(
 								res.getString(R.string.msg_result), atkRoll,
 								defRoll, secondaryRoll));
 						if (secondaryRoll > defRoll) {
 							defRoll = secondaryRoll;
 						}
 
 						// Work out the valid chits
 						// TODO: Workout the valid chits for the target armour
 					}
 				}
 			} else {
 				// this is for weapons that don't roll to hit (IVAR, APSW)
 				if (config.isInfantry() && weaponType == 1) {
 					resultOut.append(res.getString(R.string.msg_iavr_inf));
 					atkRoll = 0;
 					defRoll = 1;
 				} else {
 					resultOut.append(res.getString(R.string.msg_autohit));
 					atkRoll = 1;
 					defRoll = 0;
 				}
 			}
 			if (atkRoll > defRoll) {
 				// Hit. resolve damage
 				String[] chitList = drawChits(weapon.effectiveSize());
 
 				// If a SLAM has fired and hit spillover fire is allowed
 				if (weapon.getType().equals(Weapon.SLAM)) {
 					// Get the true range, rather than adjusted for size
 					if (whichRange == 2) {
 						result.setState(1);
 						resultOut.append(String.format(
 								res.getString(R.string.msg_spillover), 1));
 					} else if (whichRange == 3) {
 						result.setState(2);
 						resultOut.append(String.format(
 								res.getString(R.string.msg_spillover), 2));
 					}
 				}
 			} else {
 				// Missed
 				resultOut.append(res.getString(R.string.msg_missed));
 			}
 		} else {
 			resultOut.append(res.getString(R.string.msg_out_of_range));
 		}
 		result.setOutcome(resultOut.toString());
 		return result;
 	}
 
 	@Override
 	public void resolveSpillover(CombatResolutionConfig config, Integer range,
 			Integer probability) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private int fireControlToDice(String weaponType, String fireContol,
 			Integer range) {
 		int dice = 0;
 		String diceString = Utilities.RangeDie(weaponType, fireContol,
 				fireContol, range, this);
 
 		if (!diceString.equals("--")) {
 			dice = Integer.parseInt(diceString.substring(1));
 		}
 		return dice;
 	}
 
 	private int signatureToDice(Integer size) {
 		int dice = 0;
 		String diceString = "--";
 		DatabaseHelper dbh = new DatabaseHelper(this);
 		Cursor sig = dbh.getSignatureForSize(size.toString());
 		int colIndex = sig.getColumnIndex("Signature");
 		if (sig.moveToNext()) {
 			diceString = sig.getString(colIndex);
 		}
 		if (!diceString.equals("--")) {
 			dice = Integer.parseInt(diceString.substring(1));
 		}
 		return dice;
 	}
 
 	/**
 	 * Converts a Defence system reference to a die roll
 	 * 
 	 * @param which
 	 * @return
 	 */
 	private int DSToDice(Integer which) {
 		int die = 0;
 		switch (which) {
 		case 0: // None
 			die = 0;
 			break;
 		case 1: // Basic
 			die = 4;
 			break;
 		case 2: // Enhanced
 			die = 8;
 			break;
 		case 3: // Superior
 			die = 10;
 			break;
 		case 4: // Brilliant
 			die = 12;
 			break;
 		default:
 			die = 0;
 		}
 		return die;
 	}
 
 	private int ECMToDice(String ecmRating) {
 		int die = 0;
 		String diceString = "--";
 		DatabaseHelper dbh = new DatabaseHelper(this);
 		Cursor res = dbh.getDiceForECM(ecmRating);
 		int colIndex = res.getColumnIndex("Die");
 		if (res.moveToNext()) {
 			diceString = res.getString(colIndex);
 		}
 		if (!diceString.equals("--")) {
 			die = Integer.parseInt(diceString.substring(1));
 		}
 		return die;
 	}
 
 	/**
 	 * Convert the state of the target into a secondary dice
 	 * 
 	 * @param targetState
 	 * @return
 	 */
 	private int secondaryToDice(Integer targetState) {
 		int die = 0;
 		switch (targetState) {
 		case 0: // None
 			die = 0;
 			break;
 		case 1: // Hull down
 			die = 10;
 			break;
 		case 2: // Turret down
 			die = 12;
 			break;
 		case 3: // Dug in
 			die = 10;
 			break;
 		case 4: // Evading
 			die = 8;
 			break;
 		case 5: // Popping up
 			die = 6;
 			break;
 		case 6: // Soft cover
 			die = 6;
 			break;
 		default:
 			die = 0;
 		}
 		return die;
 	}
 
 	private String[] drawChits(Integer weaponSize) {
		String[] chitsDrawn = new String[weaponSize];
 		Dice dice = new Dice(120);
 
 		for (int chit = 0; chit < weaponSize; chit++) {
 			int roll = dice.roll();
 			if (roll > 0 && roll <= 10) {
 				chitsDrawn[chit] = "R3";
 			} else if (roll > 10 && roll <= 25) {
 				chitsDrawn[chit] = "R2";
 			}
 		}
 		return chitsDrawn;
 	}
 }

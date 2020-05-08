 package com.hunterdavis.thegrind;
 
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.Random;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.PointF;
 import android.net.Uri;
 import android.os.Handler;
 import android.util.AttributeSet;
 import android.util.FloatMath;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.widget.Toast;
 
 class Panel extends SurfaceView implements SurfaceHolder.Callback {
 
 	int SELECT_SHIP = 22;
 
 	InventorySQLHelper scoreData = null;
 
 	// member variables
 	private CanvasThread canvasthread;
 	public Boolean surfaceCreated;
 	public Context mContext;
 	public Boolean introScreenOver = false;
 	PointF gearTopLeft = new PointF(0, 0);
 	PointF lastPoint = new PointF(0, 0);
 	public Bitmap player1Bitmap = null;
 	public Bitmap gearBitmap = null;
 	Random myrandom = new Random();
 	int gearSize = 0;
 	int mwidth = 0;
 	int mheight = 0;
 	int fontSize = 0;
 	int currentfightStatus = 0;
 	int currentPlayerProgress = 0;
 	int currentEnemyNumber = 0;
 	int currentEnemyAttackType = 0;
 	int currentPlayerAttackType = 0;
 	int currentMonsterHP = 0;
 	int currentSliceDamage = 0;
 	int currentMonsterDamage = 0;
 	int currentQuestVisited = 0;
 	int currentQuestDefeated = 0;
 	int currentLocation = 0;
 	int currentRandomQuestItemNumber = 0;
 	int currentPlayerSpell = 0;
 	int currentEnemySpell = 0;
 	int currentPlayerPotion = 0;
	int currentPlayerHealing = 0;
 	int currentEnemyPotion = 0;
 	Player player1 = new Player();
 	float currentRotation = 0.0f;
 	Boolean autoGrind = false;
 
 	// static values that match to our static arrays
 	int numQuestDescriptions = 0;
 	int numSubQuestDescriptions = 0;
 	int numberOfEnemyDescriptions = 0;
 	int numberOfMagicHealingDescriptions = 0;
 	int numberOfHealingDescriptions = 0;
 	int numberOfEquipmentDescriptions = 0;
 
 	// tweaking for game mechanics
 	int autoGrindExpDistance = 30;
 	int autoGrindRotation = 5;
 	Point player1Buffer = new Point(5, 5);
 	int numberSubquestsInQuest = 8;
 	int numberFightsInSubQuest = 5;
 	int numberLocationsInSubQuest = 5;
 	int textColor = Color.WHITE;
 	int introTextColor = Color.BLACK;
 	int plotForwardBuffer = 500;
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		synchronized (getHolder()) {
 
 			int action = event.getAction();
 			if (action == MotionEvent.ACTION_DOWN) {
 				testAndAddAction(event.getX(), event.getY());
 
 				return true;
 			} else if (action == MotionEvent.ACTION_MOVE) {
 
 				testAndAddAction(event.getX(), event.getY());
 
 				return true;
 			} else if (action == MotionEvent.ACTION_UP) {
 
 				return true;
 			}
 			return true;
 		}
 	}
 
 	public boolean insidePlayerImageBox(float x, float y) {
 		if (introScreenOver == false) {
 			return false;
 		}
 
 		int visualDivisor = mwidth;
 		if (mheight < mwidth) {
 			visualDivisor = mheight;
 		}
 		int player1Size = visualDivisor / 8;
 
 		int top = player1Buffer.y;
 		int left = player1Buffer.x;
 		int right = left + player1Size;
 		int bottom = left + player1Size;
 
 		if (y > top) {
 			return false;
 		}
 		if (y < bottom) {
 			return false;
 		}
 		if (x > right) {
 			return false;
 		}
 		if (x < left) {
 			return false;
 		}
 		return true;
 	}
 
 	public void testAndAddAction(float x, float y) {
 		if (introScreenOver == false) {
 			return;
 		}
 
 		// quick test
 		if (y < mheight / 2) {
 			return;
 		}
 
 		if (gearSize == 0) {
 			return;
 		}
 
 		if (fdistance(x, y, gearTopLeft.x + gearSize / 2, gearTopLeft.y
 				+ gearSize / 2) > gearSize / 2) {
 			return;
 		}
 
 		if ((x == lastPoint.x) && (y == lastPoint.y)) {
 			return;
 		}
 
 		float distance = fdistance(x, y, lastPoint.x, lastPoint.y);
 		lastPoint.x = (int) x;
 		lastPoint.y = (int) y;
 		currentRotation += distance;
 		currentRotation = currentRotation % 360;
 
 		movePlotForward(distance);
 
 	}
 
 	float fdistance(float x1, float y1, float x2, float y2) {
 		return (float) FloatMath.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
 	}
 
 	public void setAutoGrind(boolean autoGrindNew) {
 		autoGrind = autoGrindNew;
 	}
 
 	public void movePlotForward(float plotDistance) {
 		currentPlayerProgress += plotDistance;
 		if (currentPlayerProgress > plotForwardBuffer) {
 			currentPlayerProgress = 0;
 			stepCharacterForward();
 		}
 	}
 
 	public void stepCharacterForward() {
 		Resources res = getResources();
 		String[] unitsarray = res.getStringArray(R.array.locations);
 
 		if (player1.statusNum == 0) {
 			// preparing for a quest
 			player1.statusNum++;
 			currentLocation = myrandom.nextInt(unitsarray.length);
 
 		} else if (player1.statusNum == 1) {
 			// traveling to adventure location
 
 			Boolean update = myrandom.nextBoolean();
 			if (update) {
 				currentLocation = myrandom.nextInt(unitsarray.length);
 
 				player1.statusNum++;
 				currentEnemyNumber = myrandom
 						.nextInt(numberOfEnemyDescriptions);
 				currentMonsterHP = myrandom.nextInt((1 + (player1.level * 3))) + 1;
 				currentQuestVisited++;
 			}
 		} else if (player1.statusNum == 2) {
 			// in a fight
 			if ((currentfightStatus != 3) && (currentfightStatus != 0)) {
 				currentfightStatus = myrandom.nextInt(2) + 1;
 			}
 			if (currentfightStatus == 0) {
 				// this is just the alert message for fight! vs monster number
 				currentfightStatus = myrandom.nextInt(2) + 1;
 			} else if (currentfightStatus == 1) {
 
 				// It is the player's turn on currentfightStatus
 				currentPlayerAttackType = myrandom.nextInt(10);
 				if (player1.currentHealth < 2) {
 					currentPlayerAttackType = 9;
 				} else if (player1.currentMana < 2) {
 					currentPlayerAttackType = 8;
 				}
 				if (currentPlayerAttackType < 4) {
 
 					// straight attack
 					currentSliceDamage = (player1.currentSword
 							+ player1.toughness + player1.level) / 3 + 1;
 					currentSliceDamage = myrandom.nextInt(currentSliceDamage) + 1;
 					currentMonsterHP -= currentSliceDamage;
 					// test if monster is dead
 					if (currentMonsterHP < 1) {
 						currentfightStatus = 3;
 					}
 				}
 				// uses magic
 				else if (currentPlayerAttackType < 8) {
 					// magic spell
 					if (player1.spells.size() >= 1) {
 						currentPlayerSpell = player1.spells.get(myrandom
 								.nextInt(player1.spells.size()));
 						int manaRequired = 2 * currentPlayerSpell;
 						if (manaRequired > player1.currentMana) {
 							currentPlayerAttackType = 8;
 							player1.currentMana += myrandom
 									.nextInt(player1.level
 											+ player1.intelligence
 											+ player1.wisdom);
 							if (player1.currentMana > player1.mana) {
 								player1.currentMana = player1.mana;
 							}
 						} else {
 							currentSliceDamage = (player1.intelligence
 									+ player1.wisdom + player1.level) / 3 + 1;
 							currentSliceDamage = myrandom
 									.nextInt(currentSliceDamage) + 1;
 							currentMonsterHP -= currentSliceDamage;
 							if (currentMonsterHP < 1) {
 								currentfightStatus = 3;
 							}
 						}
 					}
 				}
 				// restores mana
 				else if (currentPlayerAttackType < 9) {
 					String[] potionarray = res
 							.getStringArray(R.array.magichealing);
 					currentPlayerPotion = myrandom.nextInt(potionarray.length);
 					player1.currentMana += myrandom.nextInt(player1.level
 							+ player1.intelligence + player1.wisdom);
 					if (player1.currentMana > player1.mana) {
 						player1.currentMana = player1.mana;
 					}
 
 				}
 				// heals
 				else if (currentPlayerAttackType < 10) {
 					String[] healingarray = res.getStringArray(R.array.healing);
					currentPlayerHealing = myrandom.nextInt(healingarray.length);
 					player1.currentHealth += myrandom.nextInt(player1.level
 							+ player1.toughness + player1.intelligence);
 					if (player1.currentHealth > player1.health) {
 						player1.currentHealth = player1.health;
 					}
 				}
 
 			} else if (currentfightStatus == 2) {
 				// the enemy is attacking you on currentfightStatus
 				currentEnemyAttackType = myrandom.nextInt(10);
 				int randomNum = player1.level * 3;
 				if (randomNum == 0) {
 					randomNum++;
 				}
 				currentMonsterDamage = myrandom.nextInt(randomNum) + 1;
 
 				if (currentEnemyAttackType < 4) {
 					// straight attack
 					if (player1.currentHealth > currentMonsterDamage) {
 						player1.currentHealth -= currentMonsterDamage;
 					} else {
 						currentMonsterDamage = player1.currentHealth - 1;
 						player1.currentHealth -= currentMonsterDamage;
 					}
 				} else if (currentEnemyAttackType < 9) {
 					// magic spell
 					String[] magicarray = res.getStringArray(R.array.spells);
 					currentEnemySpell = myrandom.nextInt(magicarray.length);
 
 					if (player1.currentHealth > currentMonsterDamage) {
 						player1.currentHealth -= currentMonsterDamage;
 					} else {
 						currentMonsterDamage = player1.currentHealth - 1;
 					}
 				} else if (currentEnemyAttackType < 10) {
 					// enemy heals
 
					String[] potionarray = res.getStringArray(R.array.magichealing);
 					currentEnemyPotion = myrandom.nextInt(potionarray.length);
 					currentMonsterHP += myrandom.nextInt() + 1;
 				}
 			} else if (currentfightStatus == 3) {
 				player1.statusNum++;
 				int myloc = player1.level;
 				if (myloc == 0) {
 					myloc++;
 				}
 				player1.experienceUp(myrandom.nextInt(myloc) + 5);
 				currentQuestDefeated++;
 				currentfightStatus = 0;
 				currentEnemyNumber = myrandom
 						.nextInt(numberOfEnemyDescriptions);
 				currentMonsterHP = myrandom.nextInt((1 + (player1.level * 3))) + 1;
 
 				for (int i = 0; i < player1.subQuestsDefeatedThisQuest + 1; i++) {
 					if (myrandom.nextInt(10) > 3) {
 						player1.newEquipmentItem(numberOfEquipmentDescriptions);
 					}
 					player1.gold += myrandom.nextInt(myloc + 10);
 				}
 				
 				// autosave
 				saveHighScore();
 				
 			}
 		} else if (player1.statusNum == 3) {
 			// quest status that don't do anything
 			// i.e. talking to locals, searching for traps,
 			// learning spells etc any activity
 			// but you do heal
 			Boolean updater = myrandom.nextBoolean();
 			if (updater) {
 				player1.statusNum = myrandom.nextInt(2) + 1;
 			}
 			player1.currentMana += myrandom.nextInt(player1.level
 					+ player1.intelligence + player1.wisdom);
 			if (player1.currentMana > player1.mana) {
 				player1.currentMana = player1.mana;
 			}
 
 			player1.currentHealth += myrandom.nextInt(player1.level
 					+ player1.toughness + player1.intelligence);
 			if (player1.currentHealth > player1.health) {
 				player1.currentHealth = player1.health;
 			}
 
 			String[] stringsarray = res
 					.getStringArray(R.array.questincidentals);
 			currentRandomQuestItemNumber = myrandom
 					.nextInt(stringsarray.length);
 		}
 
 		// now that we've done the fighting or traveling
 		// test if we've met the criteria to beat the subquest
 		if ((numberFightsInSubQuest <= currentQuestDefeated)
 				&& (numberLocationsInSubQuest <= currentQuestVisited)) {
 			player1.subQuestsDefeatedThisQuest++;
 			currentQuestDefeated = 0;
 			currentQuestVisited = 0;
 			player1.experienceUp(myrandom.nextInt(player1.level));
 			player1.newSubQuestNum(numSubQuestDescriptions);
 			saveHighScore();
 		}
 
 		// test if we've met the criteria to beat the quest
 		if (player1.subQuestsDefeatedThisQuest >= numberSubquestsInQuest) {
 			player1.newQuestNum(numQuestDescriptions);
 			player1.experienceUp(myrandom
 					.nextInt(player1.subQuestsDefeatedThisQuest + 1) * 5);
 			player1.subQuestsDefeatedThisQuest = 0;
 			if (myrandom.nextInt(100) > 88) {
 				player1.maritalStatus++;
 			}
 			saveHighScore();
 		}
 
 	}
 
 	public void setScoreData(InventorySQLHelper scoreDataB) {
 		scoreData = scoreDataB;
 	}
 
 	public void changeName(String name) {
 		player1.name = name;
 	}
 
 	public void setPlayerUri(Uri uri) {
 		player1.changeUri(uri);
 		player1Bitmap = null;
 	}
 
 	public void reset() {
 		// reset everything
 		autoGrind = false;
 		myrandom = new Random();
 		introScreenOver = true;
 		currentPlayerProgress = 0;
 		currentPlayerAttackType = 0;
 		player1 = new Player();
 		player1Bitmap = null;
 		gearBitmap = null;
 		currentfightStatus = 0;
 		currentPlayerSpell = 0;
 		currentPlayerPotion = 0;
 		currentEnemyPotion = 0;
 		currentEnemySpell = 0;
 		currentRandomQuestItemNumber = 0;
 		currentLocation = 0;
 		currentEnemyNumber = 0;
 		currentMonsterHP = 0;
 		currentEnemyAttackType = 0;
 		currentQuestVisited = 0;
 		currentQuestDefeated = 0;
 		currentSliceDamage = 0;
 		currentMonsterDamage = 0;
 	}
 
 	public void loadGame(Player player2) {
 		player1 = player2;
 		introScreenOver = true;
 		player1Bitmap = null;
 		gearBitmap = null;
 	}
 
 	public Panel(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		mContext = context;
 		//
 		surfaceCreated = false;
 
 		initializeStringArrayLengths();
 		// reset();
 		// introScreenOver = false;
 
 		getHolder().addCallback(this);
 		setFocusable(true);
 	}
 
 	public void initializeStringArrayLengths() {
 		Resources res = getResources();
 
 		String[] unitsarray = res.getStringArray(R.array.questDescriptions);
 		numQuestDescriptions = unitsarray.length;
 		unitsarray = res.getStringArray(R.array.subquestDescriptions);
 		numSubQuestDescriptions = unitsarray.length;
 		unitsarray = res.getStringArray(R.array.enemyDescriptions);
 		numberOfEnemyDescriptions = unitsarray.length;
 		unitsarray = res.getStringArray(R.array.magichealing);
 		numberOfMagicHealingDescriptions = unitsarray.length;
 		unitsarray = res.getStringArray(R.array.healing);
 		numberOfHealingDescriptions = unitsarray.length;
 		unitsarray = res.getStringArray(R.array.enemyDescriptions);
 		numberOfEquipmentDescriptions = unitsarray.length;
 	}
 
 	public void createThread(SurfaceHolder holder) {
 		canvasthread = new CanvasThread(getHolder(), this, mContext,
 				new Handler());
 		canvasthread.setRunning(true);
 		canvasthread.start();
 	}
 
 	public void terminateThread() {
 		canvasthread.setRunning(false);
 		try {
 			canvasthread.join();
 		} catch (InterruptedException e) {
 
 		}
 	}
 
 	
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		// reset();
 		// introScreenOver = false;
 		gearBitmap = null;
 
 	}
 
 	
 	public void surfaceCreated(SurfaceHolder holder) {
 		//
 		if (surfaceCreated == false) {
 			createThread(holder);
 			// Bitmap kangoo = BitmapFactory.decodeResource(getResources(),
 			// R.drawable.kangoo);
 			surfaceCreated = true;
 		}
 	}
 
 	
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		surfaceCreated = false;
 
 	}
 
 	public int saveHighScore() {
 
 		// see if the name exists and update if so
 		boolean update = false;
 
 		SQLiteDatabase db = scoreData.getWritableDatabase();
 
 		Cursor cursor = db.query(InventorySQLHelper.TABLE, null, "name = '"
 				+ player1.name + "'", null, null, null, null);
 		if (cursor.getCount() > 0) {
 			update = true;
 		} else {
 			update = false;
 		}
 
 		cursor.close();
 
 		ContentValues values = new ContentValues();
 		values.put(InventorySQLHelper.NAME, player1.name);
 		values.put(InventorySQLHelper.EXP, player1.experience);
 		values.put(InventorySQLHelper.HEALTH, player1.health);
 		values.put(InventorySQLHelper.MANA, player1.mana);
 		values.put(InventorySQLHelper.SWORD, player1.currentSword);
 		values.put(InventorySQLHelper.ARMOR, player1.currentArmor);
 		values.put(InventorySQLHelper.HELMET, player1.currentHelmet);
 		values.put(InventorySQLHelper.GOLD, player1.gold);
 		values.put(InventorySQLHelper.MARRIED, player1.maritalStatus);
 		values.put(InventorySQLHelper.SPEED, player1.speed);
 		values.put(InventorySQLHelper.AGE, player1.age);
 		values.put(InventorySQLHelper.TOUGHNESS, player1.toughness);
 		values.put(InventorySQLHelper.INTELLIGENCE, player1.intelligence);
 		values.put(InventorySQLHelper.WISDOM, player1.wisdom);
 		values.put(InventorySQLHelper.LEVEL, player1.level);
 		if (player1.playerUri == null) {
 			values.put(InventorySQLHelper.URI, "");
 		} else {
 			values.put(InventorySQLHelper.URI, player1.playerUri.toString());
 		}
 
 		String allEquip = " ";
 		for (int i = 0; i < player1.equipment.size(); i++) {
 			allEquip += player1.equipment.get(i);
 			if (i != player1.equipment.size() - 1) {
 				allEquip += " ";
 			}
 		}
 		if (player1.equipment.size() > 0) {
 
 			values.put(InventorySQLHelper.EQUIPMENT, allEquip);
 		} else {
 			values.put(InventorySQLHelper.EQUIPMENT, "1");
 		}
 		String allSpells = " ";
 		for (int i = 0; i < player1.spells.size(); i++) {
 			allSpells += player1.spells.get(i);
 			if (i != player1.spells.size() - 1) {
 				allSpells += " ";
 			}
 		}
 		if (player1.spells.size() > 0) {
 
 			values.put(InventorySQLHelper.SPELLS, allSpells);
 		} else {
 			values.put(InventorySQLHelper.SPELLS, "4");
 		}
 
 		values.put(InventorySQLHelper.QUESTNUM, player1.questNum);
 		values.put(InventorySQLHelper.SUBQUESTNUM, player1.subQuestNum);
 		values.put(InventorySQLHelper.STATUSNUM, player1.statusNum);
 
 		long gettingitdone = -1;
 		if (update) {
 			gettingitdone = db.update(InventorySQLHelper.TABLE, values,
 					"name='" + player1.name + "'", null);
 		} else {
 			gettingitdone = db.insert(InventorySQLHelper.TABLE, null, values);
 		}
 		db.close();
 		return (int) gettingitdone;
 	}
 
 	public void updateGameState() {
 
 		if (introScreenOver == false) {
 			return;
 		}
 
 		// if autogrind is on, free exp
 		if (autoGrind == true) {
 			movePlotForward(autoGrindExpDistance);
 		}
 
 	}
 
 	@Override
 	public void onDraw(Canvas canvas) {
 
 		mwidth = canvas.getWidth();
 		mheight = canvas.getHeight();
 
 		Paint paint = new Paint();
 
 		// our player sizes should be a function both of difficulty and of
 		// screen size
 		int visualDivisor = mwidth;
 		if (mheight < mwidth) {
 			visualDivisor = mheight;
 		}
 		int player1Size = visualDivisor / 8;
 		fontSize = visualDivisor / 20;
 
 		// set up our gear
 		gearSize = 15 * mheight / 40;
 
 		if (introScreenOver == true) {
 			// draw our gear
 			if (gearBitmap == null) {
 				Bitmap _scratch = BitmapFactory.decodeResource(getResources(),
 						R.drawable.gear);
 
 				if (_scratch == null) {
 					Toast.makeText(getContext(), "WTF", Toast.LENGTH_SHORT)
 							.show();
 				}
 				gearBitmap = Bitmap.createScaledBitmap(_scratch, gearSize,
 						gearSize, false);
 			}
 			// canvas.save();
 			// canvas.rotate(currentRotation);
 			// calculate the scale - in this case = 0.4f
 
 			// test for autorotation and move x degrees
 			if (autoGrind == true) {
 				currentRotation += autoGrindRotation;
 			}
 
 			// createa matrix for the manipulation
 			Matrix matrix = new Matrix();
 			// resize the bit map if needed
 			matrix.postScale(1.0f, 1.0f);
 			// rotate the Bitmap
 			matrix.postRotate(currentRotation);
 
 			// recreate the new Bitmap
 			Bitmap resizedBitmap = Bitmap.createBitmap(gearBitmap, 0, 0,
 					gearSize, gearSize, matrix, true);
 
 			gearTopLeft.x = mwidth / 2 - resizedBitmap.getWidth() / 2;
 			gearTopLeft.y = mheight - mheight / 4 - resizedBitmap.getHeight()
 					/ 2;
 
 			canvas.drawBitmap(resizedBitmap, gearTopLeft.x, gearTopLeft.y,
 					paint);
 
 			// draw player 1
 			if (player1Bitmap == null) {
 				// cometSize = mwidth / 5;
 				// if we can't load somebody else's bitmap
 				int urilength = 0;
 				try {
 					urilength = player1.playerUri.toString().length();
 				} catch (Exception e2) {
 					// TODO Auto-generated catch block
 					player1.playerUri = null;
 				}
 
 				if (urilength > 3) {
 					InputStream photoStreamer = null;
 					Context contexter = getContext();
 					try {
 						photoStreamer = contexter.getContentResolver()
 								.openInputStream(player1.playerUri);
 					} catch (Exception e) {
 						player1.playerUri = null;
 					}
 					try {
 						photoStreamer.close();
 					} catch (Exception e1) {
 						player1.playerUri = null;
 					}
 				}
 
 				if ((player1.playerUri == null)) {
 					Bitmap _scratch = BitmapFactory.decodeResource(
 							getResources(), R.drawable.trollface);
 
 					if (_scratch == null) {
 						Toast.makeText(getContext(), "WTF", Toast.LENGTH_SHORT)
 								.show();
 					}
 
 					// now scale the bitmap using the scale value
 					player1Bitmap = Bitmap.createScaledBitmap(_scratch,
 							player1Size, player1Size, false);
 				} else {
 					// THIS IS WHERE YOU LOAD FILE URIS AT
 					InputStream photoStream = null;
 
 					Context context = getContext();
 					Boolean scratchit = false;
 					try {
 						photoStream = context.getContentResolver()
 								.openInputStream(player1.playerUri);
 					} catch (Exception e) {
 						scratchit = true;
 					}
 					if (scratchit == false) {
 						int scaleSize = decodeFile(photoStream, player1Size,
 								player1Size);
 
 						try {
 							photoStream = context.getContentResolver()
 									.openInputStream(player1.playerUri);
 						} catch (FileNotFoundException e) {
 							//
 							e.printStackTrace();
 						}
 						BitmapFactory.Options o = new BitmapFactory.Options();
 						o.inSampleSize = scaleSize;
 
 						Bitmap photoBitmap = BitmapFactory.decodeStream(
 								photoStream, null, o);
 						player1Bitmap = Bitmap.createScaledBitmap(photoBitmap,
 								player1Size, player1Size, true);
 						photoBitmap.recycle();
 					} else {
 						Bitmap _scratch = BitmapFactory.decodeResource(
 								getResources(), R.drawable.trollface);
 
 						if (_scratch == null) {
 							Toast.makeText(getContext(), "WTF", Toast.LENGTH_SHORT)
 									.show();
 						}
 
 						// now scale the bitmap using the scale value
 						player1Bitmap = Bitmap.createScaledBitmap(_scratch,
 								player1Size, player1Size, false);
 					}
 
 				}
 
 			}
 			// draw our icon
 			canvas.drawBitmap(player1Bitmap, player1Buffer.x, player1Buffer.y,
 					paint);
 
 			// Points for all our items
 			PointF levelP = new PointF(player1Buffer.x + player1Size + 3,
 					player1Buffer.y + 2 * (fontSize + 2));
 			PointF nameP = new PointF(player1Buffer.x + player1Size + 3,
 					player1Buffer.y + fontSize);
 			PointF experienceP = new PointF(mwidth / 3 - mwidth / 8 + 3,
 					mheight / 2 - mheight / 20 + 2 * mheight / 30);
 			PointF goldP = new PointF(mwidth / 3 + mwidth / 12 + mwidth / 20
 					+ 3, mheight / 2 - mheight / 20 + 2 * mheight / 30);
 			PointF healthP = new PointF(mwidth - mwidth / 3 - mwidth / 4,
 					player1Buffer.y + fontSize);
 			PointF manaP = new PointF(mwidth - mwidth / 3 - mwidth / 4,
 					player1Buffer.y + 2 * (fontSize + 2));
 			PointF swordP = new PointF(mwidth - mwidth / 3 + 3, player1Buffer.y
 					+ fontSize);
 			PointF helmP = new PointF(mwidth - mwidth / 3 + 3, player1Buffer.y
 					+ 2 * (fontSize + 2));
 			PointF armorP = new PointF(mwidth - mwidth / 3 + 3, player1Buffer.y
 					+ 3 * (fontSize + 2));
 			PointF marriageP = new PointF(mwidth - mwidth / 3 + 3, mheight / 2
 					- mheight / 3 + (fontSize + 2));
 			PointF ageP = new PointF(mwidth - mwidth / 3 + 3, mheight / 2
 					- mheight / 3 + 2 * (fontSize + 2));
 			PointF strP = new PointF(mwidth - mwidth / 3 + 3, mheight / 2
 					- mheight / 3 + 3 * (fontSize + 2));
 			PointF intP = new PointF(mwidth - mwidth / 3 + 3, mheight / 2
 					- mheight / 3 + 4 * (fontSize + 2));
 			PointF wisP = new PointF(mwidth - mwidth / 3 + 3, mheight / 2
 					- mheight / 3 + 5 * (fontSize + 2));
 			PointF speedP = new PointF(mwidth - mwidth / 3 + 3, mheight / 2
 					- mheight / 3 + 6 * (fontSize + 2));
 
 			PointF questP = new PointF(3, player1Buffer.y + 3 * (fontSize + 2));
 			PointF realQuestP = new PointF(3, player1Buffer.y + 4
 					* (fontSize + 2));
 
 			PointF subQuestP = new PointF(3, player1Buffer.y + 5
 					* (fontSize + 2));
 			PointF realSubQuestP = new PointF(3, player1Buffer.y + 6
 					* (fontSize + 2));
 
 			PointF statusP = new PointF(3, player1Buffer.y + 7 * (fontSize + 2));
 			PointF realStatusP = new PointF(3, player1Buffer.y + 8
 					* (fontSize + 2));
 			
 			PointF autoP = new PointF(3,
 					mheight / 2 - mheight / 20 - fontSize + 2 * mheight / 30);
 			
 			
 
 			// draw the level number
 			paint.setColor(textColor);
 			String local = "Level " + player1.level;
 			canvas.drawText(local, levelP.x, levelP.y, paint);
 
 			// draw the name
 			canvas.drawText(player1.name, nameP.x, nameP.y, paint);
 
 			// draw the experience
 			canvas.drawText(player1.experience + "xp", experienceP.x,
 					experienceP.y, paint);
 
 			// draw the gold
 			canvas.drawText(player1.gold + " gold", goldP.x, goldP.y, paint);
 
 			// draw the health
 			canvas.drawText(player1.currentHealth + "/" + player1.health
 					+ " hp", healthP.x, healthP.y, paint);
 
 			// draw the mana
 			canvas.drawText(player1.currentMana + "/" + player1.mana + " mp",
 					manaP.x, manaP.y, paint);
 
 			// draw the sword
 			canvas.drawText(getSwordText(), swordP.x, swordP.y, paint);
 
 			// draw the helm
 			canvas.drawText(getHelmetText(), helmP.x, helmP.y, paint);
 
 			// draw the armor
 			canvas.drawText(getArmorText(), armorP.x, armorP.y, paint);
 
 			// draw the marital status
 			canvas.drawText(getMarriageText(), marriageP.x, marriageP.y, paint);
 
 			// draw the age
 			canvas.drawText(player1.age + " years old", ageP.x, ageP.y, paint);
 
 			// draw the strength
 			canvas.drawText(player1.toughness + " str", strP.x, strP.y, paint);
 
 			// draw the int
 			canvas.drawText(player1.intelligence + " int", intP.x, intP.y,
 					paint);
 
 			// draw the wis
 			canvas.drawText(player1.wisdom + " wis", wisP.x, wisP.y, paint);
 
 			// draw the speed
 			canvas.drawText(player1.speed + " mph", speedP.x, speedP.y, paint);
 
 			// draw the current top quest
 			canvas.drawText("Quest:", questP.x, questP.y, paint);
 			canvas.drawText(getQuestText(), realQuestP.x, realQuestP.y, paint);
 
 			// draw the current sub quest
 			canvas.drawText("SubQuest:", subQuestP.x, subQuestP.y, paint);
 			canvas.drawText(getSubQuestText(), realSubQuestP.x,
 					realSubQuestP.y, paint);
 
 			// draw the current status
 			canvas.drawText("Status:", statusP.x, statusP.y, paint);
 			canvas.drawText(getStatusText(), realStatusP.x, realStatusP.y,
 					paint);
 			
 			// draw the current auto status
 			String autoGrindText = "";
 			if(autoGrind == true) {
 				autoGrindText = "AutoGrind On";
 			} else {
 				autoGrindText = "AutoGrind Off";
 			}
 			canvas.drawText(autoGrindText, autoP.x, autoP.y,
 					paint);
 			
 			
 			
 
 		} // introscreen over = true
 		else {
 			Bitmap _scratch = BitmapFactory.decodeResource(getResources(),
 					R.drawable.title);
 
 			int scaleSize = mwidth;
 			if (mwidth > mheight) {
 				scaleSize = 2 * mheight / 3;
 			}
 
 			// now scale the bitmap using the scale value
 			Bitmap newScratch = Bitmap.createScaledBitmap(_scratch, scaleSize,
 					scaleSize, false);
 			_scratch.recycle();
 
 			canvas.drawBitmap(newScratch, mwidth / 2 - scaleSize / 2, 0, paint);
 			paint.setColor(introTextColor);
 			paint.setAntiAlias(true);
 			paint.setTextSize(18);
 			canvas.drawText("Load or Start New Game", mwidth / 2 - 100, mheight
 					- mheight / 6, paint);
 			canvas.drawText("Just Click Your Menu Button", mwidth / 2 - 110, mheight
 					- mheight / 8, paint);
 		}
 
 	}
 
 	public String getStatusText() {
 		String retString = "";
 
 		if (player1.statusNum == 0) {
 			retString = "Preparing for a Quest";
 		} else if (player1.statusNum == 1) {
 			retString = "Traveling to " + getRandomLocation();
 		} else if (player1.statusNum == 2) {
 
 			switch (currentfightStatus) {
 			case 0:
 				retString = getEnemyName() + " appears";
 				break;
 			case 1:
 				// you attack
 				retString = "";
 				if (currentPlayerAttackType < 4) {
 					retString += "Attacking " + getEnemyName() + " for "
 							+ currentSliceDamage;
 				} else if (currentPlayerAttackType < 8) {
 					retString += "Casting " + getCurrentPlayerSpell() + " for "
 							+ currentSliceDamage;
 				} else if (currentPlayerAttackType < 9) {
 					retString += "Taking " + getPlayerPotionName();
 				} else if (currentPlayerAttackType < 10) {
 					retString += "Taking " + getPlayerHealingName();
 				}
 				break;
 			case 2:
 				// enemy attacks
 				retString = getEnemyName() + " ";
 				if (currentEnemyAttackType < 4) {
 					retString += "attacks for " + currentMonsterDamage;
 				} else if (currentEnemyAttackType < 8) {
 					retString += "casts " + getCurrentEnemySpell() + " for "
 							+ currentMonsterDamage;
 				} else if (currentEnemyAttackType < 10) {
 					retString += "is taking " + getEnemyPotionName();
 				}
 				break;
 			case 3:
 				retString = getEnemyName() + " is dead";
 
 				break;
 
 			default:
 				break;
 			}
 		} else if (player1.statusNum == 3) {
 			retString = getQuestIncidental();
 		}
 
 		return retString;
 	}
 
 	public String getPlayerPotionName() {
 		String retString = "";
 		Resources res = getResources();
 		String[] unitsarray = res.getStringArray(R.array.magichealing);
 		retString = unitsarray[currentPlayerPotion];
 		return retString;
 	}
 
 	public String getEnemyPotionName() {
 		String retString = "";
 		Resources res = getResources();
 		String[] unitsarray = res.getStringArray(R.array.magichealing);
 		retString = unitsarray[currentEnemyPotion];
 		return retString;
 	}
 
 	public String getPlayerHealingName() {
 		String retString = "";
 		Resources res = getResources();
 		String[] unitsarray = res.getStringArray(R.array.healing);
		retString = unitsarray[currentPlayerHealing];
 		return retString;
 	}
 
 	public String getCurrentPlayerSpell() {
 		String retString = "";
 		Resources res = getResources();
 		String[] unitsarray = res.getStringArray(R.array.spells);
 		retString = unitsarray[currentPlayerSpell];
 		return retString;
 	}
 
 	public String getCurrentEnemySpell() {
 		String retString = "";
 		Resources res = getResources();
 		String[] unitsarray = res.getStringArray(R.array.spells);
 		retString = unitsarray[currentEnemySpell];
 		return retString;
 	}
 
 	public String getEnemyName() {
 		String retString = "";
 		Resources res = getResources();
 		String[] unitsarray = res.getStringArray(R.array.enemyDescriptions);
 		int plusModifier = currentEnemyNumber - unitsarray.length;
 		if (plusModifier > -1) {
 			retString += unitsarray[unitsarray.length - 1];
 		} else {
 			retString += unitsarray[currentEnemyNumber];
 		}
 		return retString;
 	}
 
 	public String getQuestIncidental() {
 		String retString = "";
 		Resources res = getResources();
 		String[] unitsarray = res.getStringArray(R.array.questincidentals);
 		retString += unitsarray[currentRandomQuestItemNumber];
 		return retString;
 	}
 
 	public String getRandomLocation() {
 		String retString = "";
 		Resources res = getResources();
 		String[] unitsarray = res.getStringArray(R.array.locations);
 		retString += unitsarray[currentLocation];
 		return retString;
 	}
 
 	public String getQuestText() {
 		String retString = "";
 		Resources res = getResources();
 		String[] unitsarray = res.getStringArray(R.array.questDescriptions);
 		int plusModifier = player1.questNum - unitsarray.length;
 		if (plusModifier > -1) {
 			retString += unitsarray[unitsarray.length - 1];
 		} else {
 			retString += unitsarray[player1.questNum];
 		}
 		retString += ".";
 		return retString;
 	}
 
 	public String getSubQuestText() {
 		String retString = "";
 		Resources res = getResources();
 		String[] unitsarray = res.getStringArray(R.array.subquestDescriptions);
 		int plusModifier = player1.subQuestNum - unitsarray.length;
 		if (plusModifier > -1) {
 			retString += unitsarray[unitsarray.length - 1];
 		} else {
 			retString += unitsarray[player1.subQuestNum];
 		}
 		retString += ".";
 		return retString;
 	}
 
 	public String getMarriageText() {
 		String retString = "";
 		Resources res = getResources();
 		String[] unitsarray = res.getStringArray(R.array.maritalstatus);
 		int plusModifier = player1.maritalStatus - unitsarray.length;
 		if (plusModifier > -1) {
 			retString = unitsarray[unitsarray.length - 1];
 		} else {
 			retString = unitsarray[player1.maritalStatus];
 		}
 		return retString;
 	}
 
 	public String getSwordText() {
 		String retString = "";
 		Resources res = getResources();
 		String[] unitsarray = res.getStringArray(R.array.metals);
 		int plusModifier = player1.currentSword - unitsarray.length;
 		if (plusModifier > 0) {
 			retString = unitsarray[unitsarray.length - 1] + " sword +"
 					+ plusModifier;
 		} else {
 			retString = unitsarray[player1.currentSword] + " sword";
 		}
 		return retString;
 	}
 
 	public String getHelmetText() {
 		String retString = "";
 		Resources res = getResources();
 		String[] unitsarray = res.getStringArray(R.array.metals);
 		int plusModifier = player1.currentSword - unitsarray.length;
 		if (plusModifier > 0) {
 			retString = unitsarray[unitsarray.length - 1] + " helm +"
 					+ plusModifier;
 		} else {
 			retString = unitsarray[player1.currentHelmet] + " helm";
 		}
 		return retString;
 	}
 
 	public String getArmorText() {
 		String retString = "";
 		Resources res = getResources();
 		String[] unitsarray = res.getStringArray(R.array.metals);
 		int plusModifier = player1.currentSword - unitsarray.length;
 		if (plusModifier > 0) {
 			retString = unitsarray[unitsarray.length - 1] + " mail +"
 					+ plusModifier;
 		} else {
 			retString = unitsarray[player1.currentArmor] + " mail";
 		}
 		return retString;
 	}
 
 	// decodes image and scales it to reduce memory consumption
 	private int decodeFile(InputStream photostream, int h, int w) {
 		// Decode image size
 		BitmapFactory.Options o = new BitmapFactory.Options();
 		o.inJustDecodeBounds = true;
 		BitmapFactory.decodeStream(photostream, null, o);
 
 		// Find the correct scale value. It should be the power of 2.
 		int width_tmp = o.outWidth, height_tmp = o.outHeight;
 		int scale = 1;
 		while (true) {
 			if (width_tmp / 2 < w || height_tmp / 2 < h)
 				break;
 			width_tmp /= 2;
 			height_tmp /= 2;
 			scale *= 2;
 		}
 
 		return scale;
 	}
 
 } // end class

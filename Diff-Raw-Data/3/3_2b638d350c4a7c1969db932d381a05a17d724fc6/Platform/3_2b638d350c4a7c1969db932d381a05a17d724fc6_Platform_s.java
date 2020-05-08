 package com.pavlukhin.acropanda.game.level;
 
 import com.pavlukhin.acropanda.game.motion.Motion;
 import com.pavlukhin.acropanda.game.motion.MotionType;
 import com.pavlukhin.acropanda.game.hero.Sprite;
 
 import android.graphics.Canvas;
 
 public class Platform implements LevelDrawable {
 	private PlatformType type = PlatformType.NONE;
 	private Sprite sprite = null;
 	private int currentStatus = 0;
 
 	public Platform(PlatformType type) {
 		if(type == null) return;
 		this.type = type;
 		switch(type) {
 		case SIMPLE:
 			sprite = Sprite.createLru("platform_h/simple_platform.png", 4, 8);
 			break;
 		case SIMPLE_V:
 			sprite  = Sprite.createLru("platform_v/simple_platform_v.png", 1, 1);
 			break;
 		case REDUCE:
 			sprite = Sprite.createLru("platform_h/reduce_platform.png", 4, 8);
 			break;
 		case ANGLE_RIGHT:
 			sprite = Sprite.createLru("platform_h/angle_platform_right.png", 1, 8);
 			break;
 		case ANGLE_LEFT:
 			sprite = Sprite.createLru("platform_h/angle_platform_left.png", 1, 8);
 			break;
 		case THROW_OUT_RIGHT:
 			sprite = Sprite.createLru("platform_h/throw_out_platform_right.png", 1, 8);
 			break;
 		case THROW_OUT_LEFT:
 			sprite = Sprite.createLru("platform_h/throw_out_platform_left.png", 1, 8);
 			break;	
 		case TRAMPOLINE:
 			sprite = Sprite.createLru("platform_h/trampoline_platform.png",1,8);
 			break;
 		case ELECTRO:
 			sprite = Sprite.createLru("platform_h/electro_platform.png",1,4);
 			sprite.setAnimating(true);
 			break;
 		case SPRING:
 			sprite = Sprite.createLru("platform_v/spring_platform.png",2,16);
 			break;
 		case SPIKE:
 			sprite = Sprite.createLru("platform_h/spike.png",1,8);
 			sprite.setAnimating(true);
 			break;
 		case SPIKE_V:
 			sprite = Sprite.createLru("platform_v/spike_v.png", 1, 8);
 			sprite.setAnimating(true);
 			break;
         case SPIKE_UP:
             sprite = Sprite.createLru("platform_h/spike_up.png", 1, 8);
             sprite.setAnimating(true);
             break;
 		case WIN:
 			sprite = Sprite.createLru("platform_h/win_platform.png", 1, 8);
 			sprite.setAnimating(true);
 			break;
 		case TELEPORT_L_V:
 			sprite = Sprite.createLru("platform_v/teleport_l_v.png", 1, 16);
 			sprite.setAnimating(true);
 			break;
 		case TELEPORT_R_V:
 			sprite = Sprite.createLru("platform_v/teleport_r_v.png", 1, 16);
 			sprite.setAnimating(true);
 			break;
 		case SLICK:
 			sprite = Sprite.createLru("platform_h/slick.png", 1, 1);
 			break;
 		case SLOPE:
 			sprite = Sprite.createLru("platform_h/slope.png", 3, 1);
 			break;
 		case ONE_WAY_LEFT:
 			sprite = Sprite.createLru("platform_v/one_way_right.png", 1, 16);
 			break;
 		case ONE_WAY_RIGHT:
 			sprite = Sprite.createLru("platform_v/one_way_left.png", 1, 16);
 			break;
 		case ONE_WAY_DOWN:
 			sprite = Sprite.createLru("platform_h/one_way_down.png", 1, 16);
 			break;
 		case ONE_WAY_UP:
 			sprite = Sprite.createLru("platform_h/one_way_up.png", 1, 16);
 			break;
 		case SWITCH:
 			sprite = Sprite.createLru("platform_v/switch_platform.png", 4, 8);
 			break;
 		case UNLOCK:
 			sprite = Sprite.createLru("platform_v/unlock_platform.png", 2, 8);
             sprite.setAnimating(true);
 			break;
 		case UNLOCK_H:
 			sprite = Sprite.createLru("platform_h/unlock_platform.png", 2, 8);
             sprite.setAnimating(true);
 			break;
 		case STRING:
 			sprite = Sprite.createLru("platform_h/string_platform.png", 2, 16);
 			break;
 		case LIMIT:
 			sprite = Sprite.createLru("platform_v/limit_way.png", 4, 8);
 			break;
 		case BRICK:
 			sprite = Sprite.createLru("platform_h/brick.png", 4, 1);
 			break;
 		case BRICK_V:
 			sprite = Sprite.createLru("platform_v/brick_v.png", 4, 1);
 			break;
 		case GLUE:
 			sprite = Sprite.createLru("platform_h/glue.png", 1, 1);
 			break;
 		case GLUE_V:
 			sprite = Sprite.createLru("platform_v/glue_v.png", 1, 1);
 			break;
 		case TELEPORT:
 			sprite = Sprite.createLru("platform_h/teleport.png", 1, 16);
 			sprite.setAnimating(true);
 			break;
 		case INVISIBLE:
 			sprite = Sprite.createLru("platform_h/invisible_platform.png", 2, 8);
 			break;
 		case TRANSPARENT:
 			sprite = Sprite.createLru("platform_h/transparent_platform.png", 1, 8);
 			break;
 		case TRANSPARENT_V:
 			sprite = Sprite.createLru("platform_v/transparent_platform_v.png", 1, 8);
 			break;
 		case WAY_UP_DOWN:
 			sprite = Sprite.createLru("platform_h/way_up_down.png", 2, 16);
 			break;
 		case CLOUD:
 			sprite = Sprite.createLru("platform_h/cloud.png", 1, 1);
 			break;
 		case NONE:
 			break;
 		}
 	}
 	
 	public PlatformType getType() {
 		return type;
 	}
 	
 	public void updateFloor(Motion motion, Motion prevMotion) {
 		MotionType mt = motion.getType();
 		MotionType prevMt = prevMotion.getType();
 		// CLOUD
 		if(type == PlatformType.NONE) {
 			if(!mt.isCLOUD() && prevMt.isCLOUD()) {
 				type = PlatformType.CLOUD;
 				sprite = Sprite.createLru("platform_h/cloud.png", 1, 1);
 			}
 			return;
 		}
 		if(sprite == null || 
 				mt == MotionType.MAGNET || 
 				mt == MotionType.BEAT_ROOF || 
 				mt == MotionType.THROW_LEFT && motion.getStage() != 0 || 
 				mt == MotionType.THROW_RIGHT && motion.getStage() != 0 || 
 				mt == MotionType.JUMP && motion.getStage() != 0 && type != PlatformType.TRAMPOLINE ||
 				mt == MotionType.FLY_LEFT && motion.getStage() != 0 ||
 				mt == MotionType.FLY_RIGHT && motion.getStage() != 0 ||
 				mt == MotionType.TP_LEFT && prevMt == MotionType.FLY_LEFT ||
 				mt == MotionType.TP_RIGHT && prevMt == MotionType.FLY_RIGHT ||
 				mt == MotionType.TP_LR && prevMt == MotionType.FLY_LEFT ||
 				mt == MotionType.TP_RL && prevMt == MotionType.FLY_RIGHT ||
 				mt == MotionType.JUMP_LEFT_WALL && (prevMt == MotionType.FLY_LEFT || prevMt == MotionType.THROW_LEFT && prevMotion.getStage() != 0) ||
 				mt == MotionType.JUMP_RIGHT_WALL && (prevMt == MotionType.FLY_RIGHT || prevMt == MotionType.THROW_RIGHT && prevMotion.getStage() != 0) ||
 				mt == MotionType.FLY_RIGHT && ((prevMt == MotionType.FLY_LEFT || prevMt == MotionType.THROW_LEFT) && prevMotion.getStage() != 0) ||
 				mt == MotionType.FLY_LEFT && ((prevMt == MotionType.FLY_RIGHT || prevMt == MotionType.THROW_RIGHT) && prevMotion.getStage() != 0) ||
                 (mt == MotionType.STICK_LEFT || mt == MotionType.STICK_RIGHT)) {
 			return;
 		}
 		if(type == PlatformType.REDUCE) {
             if(!sprite.isAnimatingOrDelayed()) {
                 if (currentStatus < 3) {
                     sprite.changeSet(currentStatus);
                     sprite.playOnce(true);
                 }
                 if (currentStatus == 3) {
                     sprite.changeSet(currentStatus);
                     sprite.playOnce(false, new Runnable() {
                         @Override
                         public void run() {
                             type = PlatformType.NONE;
                         }
                     });
                 }
                 currentStatus++;
             }
 			return;
 		}
 		if(type == PlatformType.ANGLE_LEFT) {
 			sprite.playOnce();
 			return;
 		}
 		if(type == PlatformType.ANGLE_RIGHT) {
 			sprite.playOnce();
 			return;
 		}
 		if(type == PlatformType.THROW_OUT_LEFT) {
 			sprite.playOnce();
 			return;
 		}
 		if(type == PlatformType.THROW_OUT_RIGHT) {
 			sprite.playOnce();
 			return;
 		}
 		if(type == PlatformType.TRAMPOLINE) {
 			sprite.playOnce();
 			return;
 		}
 		if(type == PlatformType.SLOPE) {
 			switch(mt) {
 			case JUMP_LEFT:
 			case JUMP_LEFT_WALL:
 				currentStatus = 1;
 				sprite.changeSet(1);
 				break;
 			case JUMP_RIGHT:
 			case JUMP_RIGHT_WALL:
 				currentStatus = 2;
 				sprite.changeSet(2);
 				break;
 			default:
 				break;
 			}
 			return;
 		}
 		if(type == PlatformType.SIMPLE) {
 			switch(mt) {
 	//		case STEP_LEFT:
 	//			sprite.setAnimating(sprite.changeSet(1));
 	//			sprite.playOnce = true;
 	//			break;
 	//		case STEP_RIGHT:
 	//			sprite.setAnimating(sprite.changeSet(2));
 	//			sprite.playOnce = true;
 	//			break;
 			case JUMP_LEFT:
 				sprite.changeSet(1);
 				sprite.playOnce();
 				break;
 			case JUMP_RIGHT:
 				sprite.changeSet(2);
 				sprite.playOnce();
 				break;
 			case JUMP:
 			default:
 				sprite.changeSet(0);
 				sprite.playOnce();
 				break;
 			}
 			return;
 		}
 		if(type == PlatformType.ONE_WAY_DOWN && mt == MotionType.FALL) {
 			sprite.playOnce();
 			return;
 		}
 		if(type == PlatformType.WAY_UP_DOWN && mt == MotionType.FALL) {
 			sprite.changeSet(1);
 			sprite.playOnce();
 			return;
 		}
 		if(type == PlatformType.STRING) {
 //			if(mt == MotionType.STAY) {
             if(mt == MotionType.FALL) {
                 sprite.changeSet(1);
                 sprite.playOnce();
                 type = PlatformType.NONE;
 			} else if(mt == MotionType.JUMP) {
 				sprite.playOnce();
 			}
 			return;
 		}
 		if(type == PlatformType.INVISIBLE) {
             if(currentStatus == 0) {
                 sprite.playOnce(true);
                 currentStatus = 1;
             } else {
                 sprite.changeSet(1);
                 sprite.playOnce();
             }
 			return;
 		}
 		if(type == PlatformType.TRANSPARENT) {
 			sprite.playOnce();
 			type = PlatformType.NONE;
 			return;
 		}
 		// CLOUD
 		if(type == PlatformType.CLOUD) {
 			if(mt.isCLOUD()) {
 				type = PlatformType.NONE;
 				sprite = null;
 			}
 			return;
 		}
 	}
 	
 	public void updateRoof(Motion motion) {
 		MotionType mt = motion.getType();
 		int stage = motion.getStage();
 		if(mt == MotionType.BEAT_ROOF) {
 			if(type == PlatformType.SIMPLE) {
 				sprite.changeSet(3);
 				sprite.playOnce();
 			}
 			if(type == PlatformType.BRICK) {
 				if(currentStatus < 3) {
 					currentStatus++;
 					sprite.changeSet(currentStatus);
 				} else {
 					sprite = null;
 					type = PlatformType.NONE;
 				}
 			}
 			if(type == PlatformType.INVISIBLE) {
 				sprite.playOnce(currentStatus == 0);
 				currentStatus = 1;
 			}
 		}
 		if(mt == MotionType.JUMP && stage != 0 && type == PlatformType.ONE_WAY_UP) {
 			sprite.playOnce();
 		}
 		if(mt == MotionType.JUMP && stage != 0 && type == PlatformType.WAY_UP_DOWN) {
 			sprite.changeSet(0);
 			sprite.playOnce();
 		}
 		
 		if(mt == MotionType.JUMP && stage != 0 && type == PlatformType.TRANSPARENT) {
 			sprite.playOnce();
 			type = PlatformType.NONE;
 		}
 	}
 	
 	public void highlightSpring(Motion prevMotion) {
 		MotionType prevMt = prevMotion.getType();
 		if(type == PlatformType.SPRING) {
 			switch(prevMt) {
 			case JUMP:
 			case FLY_LEFT:
 			case FLY_RIGHT:
 			case THROW_LEFT:
 			case THROW_RIGHT:
 			case TP:
 				sprite.playOnce(0);
 				break;
 			default:
 				sprite.playOnce(10);
 				break;
 			}
 		}
 	}
 	
 	public void updateLeftWall(Motion motion, Motion prevMotion) {
 		MotionType mt = motion.getType();
 		int stage = motion.getStage();
 		MotionType prevMt = prevMotion.getType();
 		if(mt == MotionType.JUMP_LEFT ||
 				mt == MotionType.FLY_LEFT && stage != 0 ||
 				mt == MotionType.THROW_LEFT ||
 				mt == MotionType.JUMP_LEFT_WALL) {
 			if(type == PlatformType.ONE_WAY_LEFT) {
 				sprite.playOnce();
 			}
 			if(type == PlatformType.LIMIT && currentStatus < 3) {
 				currentStatus = currentStatus + 1;
 				sprite.playOnce(true);
 			}
 			if(type == PlatformType.TRANSPARENT_V) {
 				sprite.playOnce();
 				type = PlatformType.NONE;
 			}
 			if(type == PlatformType.SWITCH) {
 				currentStatus = (currentStatus + 1) % 4;
 				sprite.changeSet(currentStatus);
 				sprite.goToFrame(1);
 				switch(prevMt) {
 				case JUMP:
 				case FLY_LEFT:
 				case THROW_LEFT:
 				case TP:
 					sprite.playOnce(0);
 					break;
 				default:
 					sprite.playOnce(10);
 					break;
 				}
 			}
 			if(type == PlatformType.BRICK_V) {
 				if(currentStatus < 3) {
 					currentStatus++;
 					sprite.playOnce(10, true);
 				} else {
 					sprite.playOnce(10, true);
 					type = PlatformType.NONE;
 				}
 			}
 		}
 	}
 	
 	public void updateRightWall(Motion motion, Motion prevMotion) {
 		MotionType mt = motion.getType();
 		int stage = motion.getStage();
 		MotionType prevMt = prevMotion.getType();
 		if(mt == MotionType.JUMP_RIGHT ||
 				mt == MotionType.FLY_RIGHT && stage != 0 ||
 				mt == MotionType.THROW_RIGHT ||
 				mt == MotionType.JUMP_RIGHT_WALL) {
 			if(type == PlatformType.ONE_WAY_RIGHT) {
 				sprite.playOnce();
 			}
 			if(type == PlatformType.LIMIT && currentStatus < 3) {
 				currentStatus = currentStatus + 1;
 				sprite.playOnce(true);
 			}
 			if(type == PlatformType.TRANSPARENT_V) {
 				sprite.playOnce();
 				type = PlatformType.NONE;
 			}
 			if(type == PlatformType.SWITCH) {
 				currentStatus = (currentStatus + 1) % 4;
 				sprite.changeSet(currentStatus);
 				sprite.goToFrame(1);
 				switch(prevMt) {
 				case JUMP:
 				case FLY_RIGHT:
 				case THROW_RIGHT:
 				case TP:
 					sprite.playOnce(0);
 					break;
 				default:
 					sprite.playOnce(10);
 					break;
 				}
 			}
 			if(type == PlatformType.BRICK_V) {
 				if(currentStatus < 3) {
 					currentStatus++;
 					sprite.playOnce(10, true);
 				} else {
 					sprite.playOnce(10, true);
 					type = PlatformType.NONE;
 				}
 			}
 		}
 	}
 	
 	public void unlock() {
         setStatus(1);
         sprite.playOnce();
 		type = PlatformType.NONE;
 	}
 	
 	public int getStatus() {
 		return currentStatus;
 	}
 
     public void setStatus(int currentStatus) {
         this.currentStatus = currentStatus;
         sprite.changeSet(currentStatus);
     }
 
     public boolean isAnimatingOrDelayed() {
         return sprite.isAnimatingOrDelayed();
     }
 
     @Override
     public void draw(Canvas canvas, int x, int y, boolean update) {
 		if(sprite != null) {
 			if(type == PlatformType.NONE && !sprite.isAnimatingOrDelayed()) {
 				sprite = null;
 			} else {
 				sprite.onDraw(canvas, x, y, update);
 			}
 		}
 	}
 }

 package org.powerbot.game.api.wrappers.interactive;
 
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Polygon;
 
 import org.powerbot.game.api.methods.Calculations;
 import org.powerbot.game.api.methods.Game;
 import org.powerbot.game.api.methods.input.Mouse;
 import org.powerbot.game.api.methods.node.Menu;
 import org.powerbot.game.api.util.Filter;
 import org.powerbot.game.api.util.internal.Multipliers;
 import org.powerbot.game.api.util.node.Nodes;
 import org.powerbot.game.api.wrappers.Entity;
 import org.powerbot.game.api.wrappers.Identifiable;
 import org.powerbot.game.api.wrappers.Locatable;
 import org.powerbot.game.api.wrappers.RegionOffset;
 import org.powerbot.game.api.wrappers.Rotatable;
 import org.powerbot.game.api.wrappers.Tile;
 import org.powerbot.game.api.wrappers.graphics.CapturedModel;
 import org.powerbot.game.api.wrappers.graphics.model.CharacterModel;
 import org.powerbot.game.bot.Context;
 import org.powerbot.game.client.Client;
 import org.powerbot.game.client.Model;
 import org.powerbot.game.client.ModelCapture;
 import org.powerbot.game.client.RSAnimatorSequence;
 import org.powerbot.game.client.RSCharacterAnimation;
 import org.powerbot.game.client.RSCharacterHPRatio;
 import org.powerbot.game.client.RSCharacterHeight;
 import org.powerbot.game.client.RSCharacterInteracting;
 import org.powerbot.game.client.RSCharacterIsMoving;
 import org.powerbot.game.client.RSCharacterLoopCycleStatus;
 import org.powerbot.game.client.RSCharacterMessageData;
 import org.powerbot.game.client.RSCharacterOrientation;
 import org.powerbot.game.client.RSCharacterPassiveAnimation;
 import org.powerbot.game.client.RSInteractableBytes;
 import org.powerbot.game.client.RSInteractableInts;
 import org.powerbot.game.client.RSInteractableLocation;
 import org.powerbot.game.client.RSInteractableManager;
 import org.powerbot.game.client.RSInteractablePlane;
 import org.powerbot.game.client.RSInteractableRSInteractableManager;
 import org.powerbot.game.client.RSMessageDataMessage;
 import org.powerbot.game.client.RSNPCHolder;
 import org.powerbot.game.client.RSNPCNode;
 import org.powerbot.game.client.RSNPCNodeHolder;
 import org.powerbot.game.client.SequenceID;
 import org.powerbot.game.client.SequenceInts;
 
 /**
  * @author Timer
  */
 public abstract class Character implements Entity, Locatable, Rotatable, Identifiable {
 	private final Client client;
 	private final Multipliers multipliers;
 
 	public Character() {
 		this.client = Context.client();
 		this.multipliers = Context.multipliers();
 	}
 
 	public abstract int getLevel();
 
 	public abstract String getName();
 
 	public RegionOffset getRegionOffset() {
 		final RSInteractableLocation location = ((RSInteractableManager) ((RSInteractableRSInteractableManager) get()).getRSInteractableRSInteractableManager()).getData().getLocation();
 		return new RegionOffset((int) location.getX() >> 9, (int) location.getY() >> 9, getPlane());
 	}
 
 	public Tile getLocation() {
 		final RegionOffset regionTile = getRegionOffset();
 		return new Tile(Game.getBaseX() + regionTile.getX(), Game.getBaseY() + regionTile.getY(), regionTile.getPlane());
 	}
 
 	public int getPlane() {
 		return ((RSInteractablePlane) ((RSInteractableBytes) get()).getRSInteractableBytes()).getRSInteractablePlane();
 	}
 
 	public Character getInteracting() {
 		final int index = ((RSCharacterInteracting) ((RSInteractableInts) get()).getRSInteractableInts()).getRSCharacterInteracting() * multipliers.CHARACTER_INTERACTING;
 		if (index == -1) {
 			return null;
 		}
 		if (index < 0x8000) {
 			return new NPC(((RSNPCHolder) ((RSNPCNodeHolder) ((RSNPCNode) Nodes.lookup(client.getRSNPCNC(), index)).getData()).getRSNPCNodeHolder()).getRSNPC());
 		} else {
 			return new Player(client.getRSPlayerArray()[index - 0x8000]);
 		}
 	}
 
 	public int getAnimation() {
 		final Object animation = ((RSCharacterAnimation) get()).getRSCharacterAnimation();
 		if (animation != null) {
 			final Object sequence = ((RSAnimatorSequence) animation).getRSAnimatorSequence();
 			if (sequence != null) {
 				return ((SequenceID) ((SequenceInts) sequence).getSequenceInts()).getSequenceID() * multipliers.SEQUENCE_ID;
 			}
 		}
 		return -1;
 	}
 
 	public int getPassiveAnimation() {
 		final Object animation = ((RSCharacterPassiveAnimation) get()).getRSCharacterPassiveAnimation();
 		if (animation != null) {
 			final Object sequence = ((RSAnimatorSequence) animation).getRSAnimatorSequence();
 			if (sequence != null) {
 				return ((SequenceID) ((SequenceInts) sequence).getSequenceInts()).getSequenceID() * multipliers.SEQUENCE_ID;
 			}
 		}
 		return -1;
 	}
 
 	public int getHeight() {
 		return ((RSCharacterHeight) ((RSInteractableInts) get()).getRSInteractableInts()).getRSCharacterHeight() * multipliers.CHARACTER_HEIGHT;
 	}
 
 	public int getRotation() {
 		return ((RSCharacterOrientation) ((RSInteractableInts) get()).getRSInteractableInts()).getRSCharacterOrientation() * multipliers.CHARACTER_ORIENTATION;
 	}
 
 	public int getOrientation() {
 		return (180 + getRotation() * 45 / 2048) % 360;
 	}
 
 	public boolean isInCombat() {
 		return Game.isLoggedIn() && Game.getLoopCycle() < ((RSCharacterLoopCycleStatus) ((RSInteractableInts) get()).getRSInteractableInts()).getRSCharacterLoopCycleStatus() * multipliers.CHARACTER_LOOPCYCLESTATUS;
 	}
 
 	public String getMessage() {
		final Object message_data = ((RSCharacterMessageData) get()).getRSCharacterMessageData();
		if (message_data != null) {
			return (String) ((RSMessageDataMessage) message_data).getRSMessageDataMessage();
 		}
 		return null;
 	}
 
 	public int getHpPercent() {
 		final int ratio = ((RSCharacterHPRatio) ((RSInteractableInts) get()).getRSInteractableInts()).getRSCharacterHPRatio() * multipliers.CHARACTER_HPRATIO;
 		return (int) Math.ceil(isInCombat() ? (ratio * 100) / 0xff : 100);
 	}
 
 	public int getSpeed() {
 		return ((RSCharacterIsMoving) ((RSInteractableInts) get()).getRSInteractableInts()).getRSCharacterIsMoving() * multipliers.CHARACTER_ISMOVING;
 	}
 
 	public boolean isMoving() {
 		return getSpeed() != 0;
 	}
 
 	public CapturedModel getModel() {
 		final Object ref = get();
 		if (ref != null) {
 			final Model model = ModelCapture.modelCache.get(ref);
 			if (model != null) {
 				return new CharacterModel(model, this);
 			}
 		}
 		return null;
 	}
 
 	public abstract Object get();
 
 	public boolean validate() {
 		return get() != null;
 	}
 
 	public Point getCentralPoint() {
 		final CapturedModel model = getModel();
 		if (model != null) {
 			return model.getCentralPoint();
 		}
 		final RSInteractableLocation location = ((RSInteractableManager) ((RSInteractableRSInteractableManager) get()).getRSInteractableRSInteractableManager()).getData().getLocation();
 		return Calculations.groundToScreen((int) location.getX(), (int) location.getY(), Game.getPlane(), -getHeight() / 2);
 	}
 
 	public Point getNextViewportPoint() {
 		final CapturedModel model = getModel();
 		if (model != null) {
 			return model.getNextViewportPoint();
 		}
 		return getCentralPoint();
 	}
 
 	public boolean contains(final Point point) {
 		final CapturedModel model = getModel();
 		if (model != null) {
 			return model.contains(point);
 		}
 		return getLocation().contains(point);
 	}
 
 	public boolean isOnScreen() {
 		final CapturedModel model = getModel();
 		return model != null ? model.isOnScreen() : Calculations.isOnScreen(getCentralPoint());
 	}
 
 	public Polygon[] getBounds() {
 		final CapturedModel model = getModel();
 		if (model != null) {
 			return model.getBounds();
 		}
 		return getLocation().getBounds();
 	}
 
 	public boolean hover() {
 		final CapturedModel model = getModel();
 		if (model != null) {
 			return model.hover();
 		}
 		return Mouse.apply(this, new Filter<Point>() {
 			public boolean accept(final Point point) {
 				return true;
 			}
 		});
 	}
 
 	public boolean click(final boolean left) {
 		final CapturedModel model = getModel();
 		if (model != null) {
 			return model.click(left);
 		}
 		return Mouse.apply(this, new Filter<Point>() {
 			public boolean accept(final Point point) {
 				Mouse.click(true);
 				return true;
 			}
 		});
 	}
 
 	public boolean interact(final String action) {
 		final CapturedModel model = getModel();
 		if (model != null) {
 			return model.interact(action);
 		}
 		return Mouse.apply(this, new Filter<Point>() {
 			public boolean accept(final Point point) {
 				return Menu.select(action);
 			}
 		});
 	}
 
 	public boolean interact(final String action, final String option) {
 		final CapturedModel model = getModel();
 		if (model != null) {
 			return model.interact(action, option);
 		}
 		return Mouse.apply(this, new Filter<Point>() {
 			public boolean accept(final Point point) {
 				return Menu.select(action, option);
 			}
 		});
 	}
 
 	public void draw(final Graphics render) {
 		//TODO
 	}
 
 	@Override
 	public boolean equals(final Object obj) {
 		if (obj instanceof Character) {
 			final Character cha = (Character) obj;
 			return cha.get() == get();
 		}
 		return false;
 	}
 
 	@Override
 	public int hashCode() {
 		return System.identityHashCode(get());
 	}
 }

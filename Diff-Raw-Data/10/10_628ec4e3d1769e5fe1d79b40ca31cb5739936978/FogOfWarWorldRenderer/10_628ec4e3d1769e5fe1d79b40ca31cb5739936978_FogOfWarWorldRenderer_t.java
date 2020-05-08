 package org.blockout.ui;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.blockout.engine.ISpriteManager;
 import org.blockout.engine.SpriteMapping;
 import org.blockout.engine.SpriteType;
 import org.blockout.logic.FogOfWar;
 import org.blockout.world.IWorld;
 import org.blockout.world.LocalGameState;
 import org.blockout.world.Tile;
 import org.blockout.world.entity.Entity;
 import org.blockout.world.entity.Monster;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.geom.Rectangle;
 
 @Named
 public class FogOfWarWorldRenderer extends AbstractWorldRenderer {
 
 	private static final float		VIEW_DISTANCE			= 5f;
 	private static final float		SQUARED_VIEW_DISTANCE	= VIEW_DISTANCE * VIEW_DISTANCE;
 
 	private final ISpriteManager	spriteManager;
 	private final SpriteMapping		mapping;
 	private final FogOfWar			fog;
 
 	private final LocalGameState	gameState;
 
 	@Inject
 	public FogOfWarWorldRenderer(final ISpriteManager spriteManager, final IWorld world, final Camera camera,
 			final LocalGameState gameState, final FogOfWar fog) {
 		super( world, camera );
 
 		this.spriteManager = spriteManager;
 		mapping = new SpriteMapping();
 		this.fog = fog;
 		this.gameState = gameState;
 	}
 
 	@Override
 	public void render( final Graphics g ) {
 
 		camera.lock();
 		try {
 			updateFog( camera.getCenterX(), camera.getCenterY() );
			// spriteManager.startUse();
 			super.render( g );
			// spriteManager.endUse();
 
 			Image player = spriteManager.getSprite( SpriteType.Player, true );
 			player.drawCentered( camera.getHalfWidth(), camera.getHalfHeight() );
 
 		} finally {
 			camera.unlock();
 		}
 	}
 
 	@Override
 	protected void renderTile( final Graphics g, final Tile tile, final int worldX, final int worldY,
 			final int screenX, final int screenY ) {
 
 		// only render explored tiles
 		if ( !fog.isExplored( worldX, worldY ) ) {
 			return;
 		}
 
 		try {
 			SpriteType spriteType = mapping.getSpriteType( tile.getTileType() );
 			Image sprite = spriteManager.getSprite( spriteType );
 			if ( sprite != null ) {
 
 				float alpha = computeLightning( worldX, worldY, sprite );
				// spriteManager.renderInUse(spriteType, screenX, screenY);
				sprite.draw( screenX, screenY );
 
 				if ( alpha > 0.5f ) {
 					Entity e = tile.getEntityOnTile();
 					if ( e != null && e != gameState.getPlayer() ) {
 
 						spriteType = e.getSpriteType();
 						sprite = spriteManager.getSprite( spriteType, true );
 
 						computeLightning( worldX, worldY, sprite );
 						sprite.draw( screenX, screenY );
 					}
 				}
 			}
 		} catch ( IllegalArgumentException e ) {
 			// unknown sprite
 		}
 	}
 
 	private float computeLightning( final int worldX, final int worldY, final Image sprite ) {
 		double origDistX = Math.pow( worldX - camera.getCenterX(), 2 );
 		double distXP1 = Math.pow( worldX + 1 - camera.getCenterX(), 2 );
 		double origDistY = Math.pow( worldY - camera.getCenterY(), 2 );
 		double distYP1 = Math.pow( worldY + 1 - camera.getCenterY(), 2 );
 
 		float overallAlpha = 0;
 		overallAlpha += computeLightningBottomLeft( sprite, origDistX, origDistY );
 		overallAlpha += computeLightningBottomRight( distXP1, worldX, sprite, origDistY );
 		overallAlpha += computeLightningTopRight( distYP1, worldY, sprite, distXP1 );
 		overallAlpha += computeLightningTopLeft( sprite, origDistX, distYP1 );
 		return overallAlpha / 4f;
 	}
 
 	private float computeLightningBottomLeft( final Image sprite, final double origDistX, final double origDistY ) {
 		if ( origDistX + origDistY < SQUARED_VIEW_DISTANCE ) {
 			sprite.setColor( Image.BOTTOM_LEFT, 1, 1, 1, 1 );
 			return 1;
 		} else {
 			sprite.setColor( Image.BOTTOM_LEFT, 1, 1, 1, 0.5f );
 			return 0.5f;
 		}
 	}
 
 	private float computeLightningBottomRight( final double distXP1, final int worldX, final Image sprite,
 			final double origDistY ) {
 
 		if ( distXP1 + origDistY < SQUARED_VIEW_DISTANCE ) {
 			sprite.setColor( Image.BOTTOM_RIGHT, 1, 1, 1, 1 );
 			return 1;
 		} else {
 			sprite.setColor( Image.BOTTOM_RIGHT, 1, 1, 1, 0.5f );
 			return 0.5f;
 		}
 	}
 
 	private float computeLightningTopRight( final double distYP1, final int worldY, final Image sprite,
 			final double distXP1 ) {
 
 		if ( distXP1 + distYP1 < SQUARED_VIEW_DISTANCE ) {
 			sprite.setColor( Image.TOP_RIGHT, 1, 1, 1, 1 );
 			return 1;
 		} else {
 			sprite.setColor( Image.TOP_RIGHT, 1, 1, 1, 0.5f );
 			return 0.5f;
 		}
 	}
 
 	private float computeLightningTopLeft( final Image sprite, final double origDistX, final double distYP1 ) {
 		if ( origDistX + distYP1 < SQUARED_VIEW_DISTANCE ) {
 			sprite.setColor( Image.TOP_LEFT, 1, 1, 1, 1 );
 			return 1;
 		} else {
 			sprite.setColor( Image.TOP_LEFT, 1, 1, 1, 0.5f );
 			return 0.5f;
 		}
 	}
 
 	private void updateFog( final float x, final float y ) {
 
 		// TODO: move this code to the MovementHandler
 		fog.setExplored( camera.worldToTile( x - 2 ), camera.worldToTile( y - 2 ), true );
 		fog.setExplored( camera.worldToTile( x - 1 ), camera.worldToTile( y - 2 ), true );
 		fog.setExplored( camera.worldToTile( x ), camera.worldToTile( y - 2 ), true );
 		fog.setExplored( camera.worldToTile( x + 1 ), camera.worldToTile( y - 2 ), true );
 		fog.setExplored( camera.worldToTile( x + 2 ), camera.worldToTile( y - 2 ), true );
 
 		fog.setExplored( camera.worldToTile( x - 2 ), camera.worldToTile( y - 1 ), true );
 		fog.setExplored( camera.worldToTile( x - 1 ), camera.worldToTile( y - 1 ), true );
 		fog.setExplored( camera.worldToTile( x ), camera.worldToTile( y - 1 ), true );
 		fog.setExplored( camera.worldToTile( x + 1 ), camera.worldToTile( y - 1 ), true );
 		fog.setExplored( camera.worldToTile( x + 2 ), camera.worldToTile( y - 1 ), true );
 
 		fog.setExplored( camera.worldToTile( x - 2 ), camera.worldToTile( y ), true );
 		fog.setExplored( camera.worldToTile( x - 1 ), camera.worldToTile( y ), true );
 		fog.setExplored( camera.worldToTile( x ), camera.worldToTile( y ), true );
 		fog.setExplored( camera.worldToTile( x + 1 ), camera.worldToTile( y ), true );
 		fog.setExplored( camera.worldToTile( x + 2 ), camera.worldToTile( y ), true );
 
 		fog.setExplored( camera.worldToTile( x - 2 ), camera.worldToTile( y + 1 ), true );
 		fog.setExplored( camera.worldToTile( x - 1 ), camera.worldToTile( y + 1 ), true );
 		fog.setExplored( camera.worldToTile( x ), camera.worldToTile( y + 1 ), true );
 		fog.setExplored( camera.worldToTile( x + 1 ), camera.worldToTile( y + 1 ), true );
 		fog.setExplored( camera.worldToTile( x + 2 ), camera.worldToTile( y + 1 ), true );
 
 		fog.setExplored( camera.worldToTile( x - 2 ), camera.worldToTile( y + 2 ), true );
 		fog.setExplored( camera.worldToTile( x - 1 ), camera.worldToTile( y + 2 ), true );
 		fog.setExplored( camera.worldToTile( x ), camera.worldToTile( y + 2 ), true );
 		fog.setExplored( camera.worldToTile( x + 1 ), camera.worldToTile( y + 2 ), true );
 		fog.setExplored( camera.worldToTile( x + 2 ), camera.worldToTile( y + 2 ), true );
 	}
 
 	@Override
 	protected void renderTileOverlay( final Graphics g, final Tile tile, final int worldX, final int worldY,
 			final int screenX, final int screenY ) {
 		// only render explored tiles
 		if ( !fog.isExplored( worldX, worldY ) ) {
 			return;
 		}
 
 		try {
 			SpriteType spriteType = mapping.getSpriteType( tile.getTileType() );
 			Image sprite = spriteManager.getSprite( spriteType );
 			if ( sprite != null ) {
 
 				float alpha = computeLightning( worldX, worldY, sprite );
 				if ( alpha > 0.5f ) {
 					Entity e = tile.getEntityOnTile();
 					if ( e != null && e != gameState.getPlayer() ) {
 
 						if ( e instanceof Monster ) {
 
 							Monster actor = (Monster) e;
 							if ( actor.getCurrentHealth() < actor.getMaxHealth() ) {
 								int width = (int) (20f * actor.getCurrentHealth() / actor.getMaxHealth());
 
 								g.setColor( org.newdawn.slick.Color.green );
 								g.fill( new Rectangle( screenX + 6, screenY - 2, width, 4 ) );
 								g.drawRect( screenX + 6, screenY - 2, 20, 4 );
 							}
 						}
 					}
 				}
 			}
 		} catch ( IllegalArgumentException e ) {
 			// unknown sprite
 		}
 	}
 
 }

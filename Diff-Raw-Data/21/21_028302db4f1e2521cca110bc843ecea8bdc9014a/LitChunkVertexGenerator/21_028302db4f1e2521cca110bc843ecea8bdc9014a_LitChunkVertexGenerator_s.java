 package com.blarg.gdx.tilemap3d.lighting;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.math.collision.BoundingBox;
 import com.blarg.gdx.graphics.Vertices;
 import com.blarg.gdx.tilemap3d.ChunkVertexGenerator;
 import com.blarg.gdx.tilemap3d.Tile;
 import com.blarg.gdx.tilemap3d.TileChunk;
 import com.blarg.gdx.tilemap3d.TileCoord;
 import com.blarg.gdx.tilemap3d.tilemesh.TileMesh;
 
 public class LitChunkVertexGenerator extends ChunkVertexGenerator {
 	final Vector3 tmpOffset = new Vector3();
 	final Vector3 tmpLightSourcePos = new Vector3();
 	final BoundingBox tmpBounds = new BoundingBox();
 
 	@Override
 	protected void addMesh(MeshBuilder builder, Tile tile, TileMesh sourceMesh, TileChunk chunk, TileCoord position, Matrix4 transform, Color color, int firstVertex, int numVertices) {
 		// adjust position by the tilemesh offset. TileMesh's are modeled using the
 		// origin (0,0,0) as the center and are 1 unit wide/deep/tall. So, their
 		// max extents are from -0.5,-0.5,-0.5 to 0.5,0.5,0.5. For rendering
 		// purposes in a chunk, we want the extents to be 0,0,0 to 1,1,1. This
 		// adjustment fixes that
 		tmpOffset.set(TileMesh.OFFSET);
 		tmpOffset.x += (float)position.x;
 		tmpOffset.y += (float)position.y;
 		tmpOffset.z += (float)position.z;
 
 		chunk.getBoundingBoxFor(position.x, position.y, position.z, tmpBounds);
 
 		// figure out what the default lighting value is for this chunk
 		byte defaultLightValue = chunk.tileMap.skyLightValue;
 		if (chunk.tileMap.ambientLightValue > defaultLightValue)
 			defaultLightValue = chunk.tileMap.ambientLightValue;
 
 		Vertices sourceVertices = sourceMesh.getVertices();
 		sourceVertices.moveTo(firstVertex);
 
 		// copy vertices
 		for (int i = 0; i < numVertices; ++i) {
 			sourceVertices.getVertex(vertex);
 
 			// transform if applicable... (this will probably just be per-tile rotation)
 			if (transform != null) {
 				vertex.position.mul(transform);
 				vertex.normal.rot(transform);
 			}
 
 			// translate vertex into "world/tilemap space"
 			vertex.position.add(tmpOffset);
 
 			// now we need to find the appropriate light source for this vertex. this light source could be either
 			// this very tile itself, or an adjacent tile. we'll check using the vertex normal as a direction to
 			// "look in" for the light source ...
 
 			// get the exact "world/tilemap space" position to grab a potential light source tile from
			tmpLightSourcePos.set(vertex.position).add(vertex.normal);

			// HACK: if the light source position we just got lies exactly on the max x/y/z boundaries of this tile
			//       then casting it to int and using it as a tilemap grid coord to get the tile will fetch us the
			//       _next_ tile which we probably don't want when the position is exactly at max x/y/z ...
			//       (this was resulting in some incorrect dark edges along certain tile layouts)
			if (tmpLightSourcePos.x == tmpBounds.max.x)
				tmpLightSourcePos.x -= 0.01f;
			if (tmpLightSourcePos.y == tmpBounds.max.y)
				tmpLightSourcePos.y -= 0.01f;
			if (tmpLightSourcePos.z == tmpBounds.max.z)
				tmpLightSourcePos.z -= 0.01f;
 
 			float brightness;
 
 			// if the light source position is off the bounds of the entire world then use the default light value.
 			// the below call to TileChunk.getWithinSelfOrNeighbour() actually does do bounds checking, but we would
 			// need to cast from float to int first. this causes some issues when the one or more of the lightSource
 			// x/y/z values are between 0 and -1 (rounds up to 0 when using a cast). rather then do some weird custom
 			// rounding, we just check for negatives to ensure we catch them and handle it properly
 			// NOTE: this is _only_ a problem currently because world coords right now are always >= 0
 			if (tmpLightSourcePos.x < 0.0f || tmpLightSourcePos.y < 0.0f || tmpLightSourcePos.z < 0.0f)
 				brightness = Tile.getBrightness(defaultLightValue);
 			else {
 				// light source is within the boundaries of the world, get the
 				// actual tile (may or may not be in a neighbouring chunk)
 				int lightX = (int)tmpLightSourcePos.x - chunk.getMinX();
 				int lightY = (int)tmpLightSourcePos.y - chunk.getMinY();
 				int lightZ = (int)tmpLightSourcePos.z - chunk.getMinZ();
 
 				Tile lightSourceTile = chunk.getWithinSelfOrNeighbourSafe(lightX, lightY, lightZ);
 				if (lightSourceTile == null)
 					// out of bounds of the map
 					brightness = Tile.getBrightness(defaultLightValue);
 				else if (lightSourceTile.isEmptySpace())
 					// this tile is getting it's light from another tile that is empty
 					// just use the other tile's light value as-is
 					brightness = lightSourceTile.getBrightness();
 				else {
 					// this tile is getting it's light from another tile that is not empty
 					// check if the direction we went in to find the other tile passes through any
 					// of the other tile's opaque sides. if so, we cannot use its light value and
 					// should instead just use whatever this tile's light value is
 					// TODO: i'm pretty sure this is going to produce poor results at some point in the future... fix!
 
 					TileMesh lightSourceTileMesh = chunk.tileMap.tileMeshes.get(lightSourceTile);
 
 					// collect a list of the sides to check for opaqueness with the light source tile .. we check
 					// the sides of the light source mesh opposite to the direction of the vertex normal (direction we
 					// are "moving" in)
 					// TODO: is it better to check each side individually? how would that work if the normal moves us
 					//       in a non-orthogonal direction and we need to check 2 sides ... ?
 					byte sides = 0;
 					if (vertex.normal.y < 0.0f) sides |= TileMesh.SIDE_TOP;
 					if (vertex.normal.y > 0.0f) sides |= TileMesh.SIDE_BOTTOM;
 					if (vertex.normal.x < 0.0f) sides |= TileMesh.SIDE_RIGHT;
 					if (vertex.normal.x > 0.0f) sides |= TileMesh.SIDE_LEFT;
 					if (vertex.normal.z < 0.0f) sides |= TileMesh.SIDE_BACK;
 					if (vertex.normal.z > 0.0f) sides |= TileMesh.SIDE_FRONT;
 
 					if (lightSourceTileMesh.isOpaque(sides))
 						brightness = tile.tileLight;
 					else
 						brightness = lightSourceTile.getBrightness();
 				}
 			}
 
 			// TODO: need to play with vertex/mesh color combinations a bit more to see if this is really correct
 			vertex.color.set(
 					vertex.color.r * color.r * brightness,
 					vertex.color.g * color.g * brightness,
 					vertex.color.b * color.b * brightness,
 					vertex.color.a * color.a
 			);
 
 			builder.vertex(vertex);
 			sourceVertices.moveNext();
 		}
 	}
 }

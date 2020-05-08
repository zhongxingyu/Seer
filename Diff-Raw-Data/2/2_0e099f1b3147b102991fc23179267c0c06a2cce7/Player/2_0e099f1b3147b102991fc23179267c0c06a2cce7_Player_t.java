 package com.colonycraft.world.entities;
 
 import java.util.List;
 
 import javax.vecmath.Vector3f;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.GL11;
 
 import com.bulletphysics.collision.shapes.CapsuleShape;
 import com.bulletphysics.dynamics.RigidBody;
 import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
 import com.bulletphysics.linearmath.DefaultMotionState;
 import com.bulletphysics.linearmath.MotionState;
 import com.bulletphysics.linearmath.Transform;
 import com.colonycraft.ColonyCraft;
 import com.colonycraft.math.AABB;
 import com.colonycraft.math.MathHelper;
 import com.colonycraft.math.Vec3f;
 import com.colonycraft.math.Vec3i;
 import com.colonycraft.rendering.camera.FirstPersonCamera;
 import com.colonycraft.world.Block;
 import com.colonycraft.world.BlockRayCastCalculator;
 import com.colonycraft.world.LightProcessor;
 import com.colonycraft.world.BlockRayCastCalculator.Intersection;
 import com.colonycraft.world.ChunkData;
 import com.colonycraft.world.World;
 import com.colonycraft.world.blocks.BlockManager;
 
 public class Player extends Entity
 {
 	private FirstPersonCamera camera;
 
 	private static final float SQRT_2_INV = 1.0f / MathHelper.sqrt(2.0f);
 	private static final float MAX_SPEED = 7.0f;
 	private static final float RADIUS = 0.35f;
 	private static final float HEIGHT = 1.75f;
 	private static final float EYE_HEIGHT = 1.60f;
 	private static final float RAY_LENGTH = 5.0f;
 
 	private float angle;
 	private float tilt;
 	private Vector3f velocity;
 	private Vector3f vec0;
 	private Vec3f vec1;
 	private AABB rayAABB;
 	private AABB blockAABB;
 
 	private RigidBody body;
 	private MotionState motionState;
 
 	private Vec3i selectedBlock;
 	private Vec3i adjacentBlock;
 	private boolean rayHit;
 	private AABB selectedBlockAABB;
 	
 	private float buildTimeout;
 	private boolean flying = true;
 
 	public Player(World world)
 	{
 		super(world);
 		camera = new FirstPersonCamera();
 		camera.setNearAndFar(0.2f, world.getViewingDistance());
 
 		transform = new Transform();
 		transform.origin.set(1.5f, 40, 1.5f);
 		velocity = new Vector3f();
 		vec0 = new Vector3f();
 		vec1 = new Vec3f();
 		rayAABB = new AABB(new Vec3f(), new Vec3f());
 		blockAABB = new AABB(new Vec3f(), new Vec3f());
 		selectedBlock = new Vec3i();
 		adjacentBlock = new Vec3i();
 		selectedBlockAABB = new AABB(new Vec3f(), new Vec3f());
 
 		createBody();
 
 		Mouse.setGrabbed(true);
 	}
 
 	public void setBody(RigidBody body)
 	{
 		this.body = body;
 		this.motionState = body.getMotionState();
 	}
 
 	private void createBody()
 	{
 		float mass = 65.0f;
 		CapsuleShape shape = new CapsuleShape(RADIUS, HEIGHT - RADIUS * 2);
 
 		Vector3f localInertia = new Vector3f(0, 0, 0);
 		shape.calculateLocalInertia(mass, localInertia);
 
 		motionState = new DefaultMotionState();
 		motionState.setWorldTransform(transform);
 
 		RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, motionState, shape, localInertia);
 		rbInfo.friction = 1.0f;
 		body = new RigidBody(rbInfo);
 		body.setSleepingThresholds(0.01f, 0.0f);
 		body.setAngularFactor(0.0f);
 		body.setRestitution(0.0f);
 		body.setCcdMotionThreshold(3.0f);
 		body.setDamping(0.2f, 0.0f);
 
 		world.getPhysicsManager().addRigidBody(body);
 	}
 
 	@Override
 	public void update()
 	{
 		float step = ColonyCraft.getIntance().getStep();
 		motionState.getWorldTransform(transform);
 
 		movement(step);
 		rayCast();
 		
 		if (Keyboard.isKeyDown(Keyboard.KEY_A))
 		{
 			if (rayHit && buildTimeout < 0.0f)
 			{
 				world.setBlockAt(selectedBlock.x, selectedBlock.y, selectedBlock.z, BlockManager.getBlock("Air"));
 				buildTimeout = 0.1f;
 			}
 		} 
 		if (Mouse.isButtonDown(0))
 		{
 			if (rayHit && buildTimeout < 0.0f)
 			{
 				System.out.printf("build at: %d %d %d%n", adjacentBlock.x, adjacentBlock.y, adjacentBlock.z);
 				world.setBlockAt(adjacentBlock.x, adjacentBlock.y, adjacentBlock.z, BlockManager.getBlock("Dirt"));
 				buildTimeout = 0.1f;
 			}
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_F))
 		{
 			flying ^= true;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_L))
 		{
 			if (buildTimeout < 0.0f)
 			{
 				int block = 0xF & world.getLightAt(adjacentBlock.x, adjacentBlock.y, adjacentBlock.z, false);
 				if (block == LightProcessor.MAX_LIGTH)
 				{
 					world.getLightProcessor().unspreadLightB(adjacentBlock.x, adjacentBlock.y, adjacentBlock.z, LightProcessor.MAX_LIGTH);
 				} else
 				{
 					world.getLightProcessor().spreadLightB(adjacentBlock.x, adjacentBlock.y, adjacentBlock.z, LightProcessor.MAX_LIGTH);
 				}
 				buildTimeout = 0.5f;
 			}
 		}
 		
 		buildTimeout -= step;
 		
 
 		/* Update frustum */
 		camera.lookTrough();
 	}
 
 	private void movement(float step)
 	{
 		int s = 0;
 		int m = 0;
 		int u = 0;
 
 		if (Keyboard.isKeyDown(Keyboard.KEY_D))
 			s--;
 		if (Keyboard.isKeyDown(Keyboard.KEY_Q))
 			s++;
 		if (Keyboard.isKeyDown(Keyboard.KEY_Z))
 			m++;
 		if (Keyboard.isKeyDown(Keyboard.KEY_S))
 			m--;
 		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
 			u++;
 		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
 			u--;
 
 		float sin = MathHelper.sin(angle);
 		float cos = MathHelper.cos(angle);
 
 		body.getLinearVelocity(velocity);
 		float veloY = velocity.y;
 		velocity.y = 0;
 
 		boolean onGround = flying || Math.abs(veloY) < 0.48f && Math.abs((float) MathHelper.round(transform.origin.y - HEIGHT * 0.5f) - (transform.origin.y - HEIGHT * 0.5f)) < 0.04f;
 
 		int l = s * s + m * m;
 		vec0.set(s * cos + m * sin, 0, m * cos - s * sin);
 		if (l == 2)
 		{
 			vec0.scale(SQRT_2_INV);
 		}
 		vec0.scale(MAX_SPEED);
 
 		vec0.sub(velocity);
 		if (!onGround)
 		{
 			vec0.scale(0.1f);
 		}
 		vec0.scale(300);
 		if (u == 1 && onGround)
 		{
 			vec0.y = 30000;
 			if (flying)
 			{
 				vec0.y = 500;
 			}
 		}
 		if (flying)
 		{
 			vec0.y += 9.81 / body.getInvMass();
 			if (u == -1)
 			{
 				vec0.y -= 500;
 			}
 		}
 
 		body.applyCentralForce(vec0);
 
 		camera.setPos(transform.origin.x, transform.origin.y + EYE_HEIGHT - (HEIGHT * 0.5f), transform.origin.z);
 
 		int r = -Mouse.getDX();
 		tilt += Mouse.getDY() * 0.01f;
		angle += r * 0.5f;
 		tilt = MathHelper.clamp(tilt, -MathHelper.f_PI_div_2 + 0.0001f, MathHelper.f_PI_div_2 - 0.0001f);
 		camera.setDir(sin, MathHelper.tan(tilt), cos);
 	}
 
 	private void rayCast()
 	{
 		vec1.set(camera.getDir());
 		vec1.normalize();
 		vec1.scale(RAY_LENGTH * 0.5f);
 		rayAABB.getDimensions().set(Math.abs(vec1.x), Math.abs(vec1.y), Math.abs(vec1.z));
 		vec1.add(camera.getPos());
 		rayAABB.setPosition(vec1);
 
 		BlockRayCastCalculator caster = world.getBlockRayCastCalculator();
 
 		int minX = MathHelper.floor(rayAABB.minX());
 		int maxX = MathHelper.ceil(rayAABB.maxX());
 		int minY = MathHelper.floor(rayAABB.minY());
 		int maxY = MathHelper.ceil(rayAABB.maxY());
 		int minZ = MathHelper.floor(rayAABB.minZ());
 		int maxZ = MathHelper.ceil(rayAABB.maxZ());
 
 		Intersection closestIntersection = null;
 		
 		for (int x = minX; x <= maxX; ++x)
 		{
 			for (int y = minY; y <= maxY; ++y)
 			{
 				for (int z = minZ; z <= maxZ; ++z)
 				{
 					int blockData = world.getBlockAt(x, y, z, false);
 					if (blockData == -1)
 						continue;
 					Block bl = BlockManager.getBlock(ChunkData.getType(blockData));
 					int faces = ChunkData.getFaceMask(blockData);
 					if (bl.raycasts() && faces != 0)
 					{
 						blockAABB.set(bl.getAABB());
 						blockAABB.getPosition().add(x, y, z);
 						blockAABB.getPosition().add(0.5f, 0.5f, 0.5f);
 						blockAABB.recalcVertices();
 						List<BlockRayCastCalculator.Intersection> intersections = caster.executeIntersection(x, y, z, blockAABB, camera.getPos(), camera.getDir(), faces);
 						if (intersections.size() > 0)
 						{
 							Intersection in = intersections.get(0);
 							if (in.getDistance() < RAY_LENGTH && (closestIntersection == null || in.getDistance() < closestIntersection.getDistance()))
 							{
 								closestIntersection = in;
 								selectedBlockAABB.set(blockAABB);
 							}
 						}
 
 					}
 				}
 			}
 		}
 
 		if (closestIntersection != null)
 		{
 			selectedBlock.set(closestIntersection.getBlockPosition());
 			adjacentBlock.set(closestIntersection.calcAdjacentBlockPos());
 			rayHit = true;
 		} else
 		{
 			rayHit = false;
 		}
 
 	}
 
 	@Override
 	public void render()
 	{		
 		if (rayHit)
 		{
 			GL11.glDisable(GL11.GL_DEPTH_TEST);
 			GL11.glEnable(GL11.GL_BLEND);
 			selectedBlockAABB.render(0.0f, 0.0f, 0.0f, 0.2f);
 			GL11.glEnable(GL11.GL_DEPTH_TEST);
 			GL11.glDisable(GL11.GL_BLEND);
 		}
 
 	}
 
 	public FirstPersonCamera getCamera()
 	{
 		return camera;
 	}
 
 }

 package info.aoisensi.skinplus.client.renderer.entity;
 
 import static net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED;
 import static net.minecraftforge.client.IItemRenderer.ItemRendererHelper.BLOCK_3D;
 
 import java.lang.reflect.Field;
 import java.nio.channels.Channels;
 
 import info.aoisensi.skinplus.SkinPlus;
 import info.aoisensi.skinplus.SkinPlusDownloader;
 import info.aoisensi.skinplus.client.model.ModelPlusBiped;
 
 import org.lwjgl.opengl.GL11;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.client.entity.EntityPlayerSP;
 import net.minecraft.client.model.ModelBiped;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.entity.RenderLiving;
 import net.minecraft.client.renderer.entity.RenderPlayer;
 import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumAction;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemArmor;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 import net.minecraft.scoreboard.Score;
 import net.minecraft.scoreboard.ScoreObjective;
 import net.minecraft.scoreboard.Scoreboard;
 import net.minecraft.util.MathHelper;
 import net.minecraftforge.client.ForgeHooksClient;
 import net.minecraftforge.client.IItemRenderer;
 import net.minecraftforge.client.MinecraftForgeClient;
 
 
 @SideOnly(Side.CLIENT)
 public class RenderPlusPlayer extends RenderPlayer
 {
 	
 	private ModelBiped modelBipedMainSub;
 	
 	private  static Field fieldMainModel, fieldModelBipedMain;
 	
 	public RenderPlusPlayer()
 	{
 		super();
 		this.modelBipedMainSub = (ModelBiped)this.mainModel;
 	}
 	
 	public void changeModelBiped(EntityPlayer par1EntityPlayer)
 	{
 		String username = par1EntityPlayer.username;
 		if(SkinPlus.instance.RenderPlusMap.containsKey(username))
 		{
 			ModelPlusBiped modelPlusBiped = SkinPlus.instance.RenderPlusMap.get(username);
 			if(modelPlusBiped != null) {
				//DLされたらDLするスレッドから辞書に追記する
 				changeModelBiped((ModelBiped)modelPlusBiped);
 			} else {
				//nullならDL中orDL失敗
 				changeModelBiped(modelBipedMainSub);
 			}
 		} else {
			//存在してなかったらとりあえずnullを入れとく
 			SkinPlus.instance.RenderPlusMap.put(username, null);
			//んで、DL開始
 			SkinPlusDownloader skinPlusDownloader = new SkinPlusDownloader(username);
 			skinPlusDownloader.start();
 			
 			changeModelBiped(modelBipedMainSub);
 		}
 	}
 	
 	public void changeModelBiped(ModelBiped modelBiped)
 	{
 		try {
 			if(fieldMainModel == null) {
 				fieldMainModel = RenderLiving.class.getDeclaredField("mainModel");
 				fieldMainModel.setAccessible(true);
 			}
 			if(fieldModelBipedMain == null) {
 				fieldModelBipedMain = RenderPlayer.class.getDeclaredField("modelBipedMain");
 				fieldModelBipedMain.setAccessible(true);
 			}
 		} catch (NoSuchFieldException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			fieldMainModel.set(this, modelBiped);
 			fieldModelBipedMain.set(this, modelBiped);
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 	}
 
 	
 	public void renderPlayer(EntityPlayer par1EntityPlayer, double par2, double par4, double par6, float par8, float par9)
 	{
 		changeModelBiped(par1EntityPlayer);
 		super.renderPlayer(par1EntityPlayer, par2, par4, par6, par8, par9);
 	}
 	
 	public void renderFirstPersonArm(EntityPlayer par1EntityPlayer)
 	{
 		changeModelBiped(par1EntityPlayer);
 		super.renderFirstPersonArm(par1EntityPlayer);
 	}
 }

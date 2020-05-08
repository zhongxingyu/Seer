 package me.exphc.BugTest1;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.UUID;
 import java.util.Iterator;
 import java.util.logging.Logger;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.Formatter;
 import java.lang.Byte;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.io.*;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.*;
 import org.bukkit.event.*;
 import org.bukkit.event.block.*;
 import org.bukkit.event.player.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.Material.*;
 import org.bukkit.material.*;
 import org.bukkit.block.*;
 import org.bukkit.entity.*;
 import org.bukkit.command.*;
 import org.bukkit.inventory.*;
 import org.bukkit.configuration.*;
 import org.bukkit.configuration.file.*;
 import org.bukkit.scheduler.*;
 import org.bukkit.enchantments.*;
 import org.bukkit.*;
 
 import net.minecraft.server.CraftingManager;
 
 public class BugTest1 extends JavaPlugin {
     Logger log = Logger.getLogger("Minecraft");
 
     public void onEnable() {
        net.minecraft.server.CraftingManager.getInstance().registerShapelessRecipe(
            new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOL, 1, net.minecraft.server.BlockCloth.e(2)),
            new Object[] { 
                new net.minecraft.server.ItemStack(net.minecraft.server.Block.CACTUS, 1, 0),
                new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOL, 1, 0)
            });
     }
 
     public void onDisable() {
     }
 }

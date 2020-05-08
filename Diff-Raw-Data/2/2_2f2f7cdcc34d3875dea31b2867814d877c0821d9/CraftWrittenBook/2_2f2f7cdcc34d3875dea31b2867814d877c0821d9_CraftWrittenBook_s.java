 package com.p000ison.dev.copybooks.api;
 
 import net.minecraft.server.NBTTagCompound;
 import net.minecraft.server.NBTTagList;
 import net.minecraft.server.NBTTagString;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Represents a CraftWrittenBook
  */
 public class CraftWrittenBook implements WrittenBook {
     private NBTTagCompound tag;
     private String title;
     private String author;
     private ArrayList<String> pages;
 
 
     public CraftWrittenBook(String title, String author, ArrayList<String> pages, String creator)
     {
         this.title = title;
         this.author = author;
         this.pages = pages;
     }
 
     public CraftWrittenBook(ItemStack itemStack) throws InvalidBookException
     {
         this((CraftItemStack) itemStack);
     }
 
     public CraftWrittenBook(CraftItemStack itemStack) throws InvalidBookException
     {
 
        if (itemStack.getTypeId() != 386) {
             throw new InvalidBookException("The book must be a written book!");
         }
 
         tag = itemStack.getHandle().getTag();
 
         if (tag == null) {
             tag = new NBTTagCompound();
         }
 
         author = tag.getString("author");
         title = tag.getString("title");
 
         NBTTagList pages = tag.getList("pages");
         ArrayList<String> realPages = new ArrayList<String>();
 
         for (int i = 0; i < pages.size(); i++) {
             String page = pages.get(i).getName();
             if (page.length() > 256) {
                 throw new InvalidBookException("The maximum size of a page is 256!");
             }
             realPages.add(page);
         }
 
         this.pages = realPages;
     }
 
     @Override
     public String getTitle()
     {
         return title;
     }
 
     @Override
     public String getAuthor()
     {
         return author;
     }
 
     @Override
     public ArrayList<String> getPages()
     {
         return pages;
     }
 
     @Override
     public void setTitle(String title)
     {
         this.title = title;
     }
 
     @Override
     public void setAuthor(String author)
     {
         this.author = author;
     }
 
     @Override
     public void setPages(ArrayList<String> pages)
     {
         this.pages = pages;
     }
 
     @Override
     public boolean unsign()
     {
         if (tag.get("author") == null || tag.getString("title") == null) {
             return false;
         }
 
         tag.remove("author");
         tag.remove("title");
 
         return true;
     }
 
     public ItemStack toItemStack(int amount) throws InvalidBookException
     {
         CraftItemStack item = new CraftItemStack(Material.WRITTEN_BOOK);
         NBTTagCompound newBookData = new NBTTagCompound();
 
         newBookData.setString("author", this.getAuthor());
         newBookData.setString("title", this.getTitle());
 
         NBTTagList pages = new NBTTagList();
 
         List<String> bookPages = this.getPages();
 
         for (int i = 0; i < bookPages.size(); i++) {
             String page = bookPages.get(i);
             if (page.length() > 256) {
                 throw new InvalidBookException("The maximum size of a page is 256!");
             }
             pages.add(new NBTTagString(String.valueOf(i), page));
         }
 
         newBookData.set("pages", pages);
 
         item.getHandle().tag = newBookData;
 
         return item;
     }
 }

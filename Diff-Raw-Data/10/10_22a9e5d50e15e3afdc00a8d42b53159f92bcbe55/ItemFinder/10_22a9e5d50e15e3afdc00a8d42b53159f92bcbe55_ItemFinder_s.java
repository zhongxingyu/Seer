 package com.blogspot.aptgetmoo.dhjclient.item;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.safety.Whitelist;
 import org.jsoup.select.Elements;
 
 import com.blogspot.aptgetmoo.dhjclient.parser.Webpage;
 
 /**
  * @author Dzul Nizam
  */
 public class ItemFinder implements IItemFinder {
 
     private final static int DEFAULT_ITEMS_PER_PAGE = 20;
 
     private final Webpage mResultPage;
 
     private int mItemsPerPage;
 
     private ItemType mItemType;
 
     private int mTotalResult;
 
     /**
      * @param	pResultPage
      */
     public ItemFinder(Webpage pResultPage) {
         mResultPage = pResultPage;
         mItemsPerPage = DEFAULT_ITEMS_PER_PAGE;
     }
 
     /**
      * @param	pResultPage
      * @param	pItemsPerPage
      */
     public ItemFinder(Webpage pResultPage, int pItemsPerPage) {
         mResultPage = pResultPage;
         mItemsPerPage = pItemsPerPage;
     }
 
     @Override
     public ArrayList<Item> find(final String keyword,
             final ItemType type, final int page) throws IOException {
 
         mTotalResult = -1;
         mItemType = type;
 
         return parse(keyword, type, page);
     }
 
     @Override
     public int getTotalPage() {
         if (mTotalResult == -1) return 0;
 
         return (int) Math.ceil((float) mTotalResult / mItemsPerPage);
     }
 
     @Override
     public int getResultCount() {
         return mTotalResult;
     }
 
     @Override
     public int getItemsPerPage() {
         return mItemsPerPage;
     }
 
     /**
      * @param	pItemsPerPage
      */
     public void setItemsPerPage(int pItemsPerPage) {
         mItemsPerPage = pItemsPerPage;
     }
 
     private ArrayList<Item> parse(final String keyword,
             final ItemType type, final int page) throws IOException {
 
         Document doc = null;
         ArrayList<Item> dataHolders = null;
 
         ((ResultPage) mResultPage).setFetchParameters(keyword, type.getQueryString(), page);
         doc = Jsoup.parse(mResultPage.getParseable());
 
         dataHolders = new ArrayList<Item>();
 
         if (doc != null && (mTotalResult = parseTotalResult(doc)) > 0) {
             // final Elements rows = doc.select("[name=hdnCounter] + table tr");
             final Element input = doc.select("input[name=hdnCounter").first();
             Element table = null;
             if (input != null && (table = input.parent()) != null) {
                 final Elements rows = table.select("tr");
                 dataHolders = parseRows(rows);
             }
         }
 
         return dataHolders;
     }
 
     private int parseTotalResult(Document doc) {
         Elements reuseElems;
         Element reuseElem;
 
         // First table
         reuseElems = doc.select("body > table > tbody > tr");
         if (reuseElems.size() < 2) return 0;
 
         reuseElem = reuseElems.get(1).select("> td").first();
         if (reuseElem == null) return 0;
 
         // Second table
         reuseElems = reuseElem.select("> table > tbody > tr");
         if (reuseElems.size() < 2) return 0;
 
         reuseElem = reuseElems.get(1).select("td").first();
         if (reuseElem == null) return 0;
 
         // Third table
         reuseElems = reuseElem.select("> table > tbody > tr");
         if (reuseElems.size() < 1) return 0;
 
         reuseElems = reuseElems.get(0).select("> td");
         if (reuseElems.size() < 5) return 0;
 
         reuseElem = reuseElems.get(mItemType.ordinal());
         if (reuseElem == null) return 0;
 
         // Fourth table
         reuseElems = reuseElem.select("> table > tbody > tr");
         if (reuseElems.size() < 1) return 0;
 
         reuseElem = reuseElems.get(0);
         if (reuseElem == null) return 0;
 
         reuseElems = reuseElem.select("> td");
         if (reuseElems.size() < 3) return 0;
 
         String text = reuseElems.get(1).text();
         if (text.length() == 0) return 0;
 
         final int start = text.indexOf('(');
         final int end = text.indexOf(')');
 
         if (start != -1 && end != -1 && start < end - 1) {
             try {
                 text = text.substring(start + 1, end);
                 return Integer.valueOf(text);
             } catch (NumberFormatException e) {
                 return 0;
             }
         }
 
         return 0;
     }
 
     private ArrayList<Item> parseRows(Elements rows) {
         final int rowSize = rows.size();
 
         ArrayList<Item> dataHolders = null;
         Item dataHolder = null;
 
         if (rowSize > 1) {
             dataHolders = new ArrayList<Item>();
 
             for (int i = 1; i < rowSize; i++) {
                 final Element currentRow = rows.get(i);
                 final Elements cols = currentRow.select("td");
 
                 if (cols.size() == 4) {
                     dataHolder = parseDataCol(cols.get(1));
                     dataHolder.itemSequence = parseSequence(cols.get(0));
                     dataHolder.itemExpiryDate = parseExpiredDate(cols.get(2));
                     dataHolder.itemCompanyCode = parseCompCode(cols.get(3));
                     dataHolder.itemType = mItemType;
 
                     dataHolders.add(dataHolder);
                 }
             }
         }
 
         return dataHolders.size() == 0 ? null : dataHolders;
     }
 
     private int parseSequence(Element pCol) {
         int sequence = -1;
 
         try {
             sequence = Integer.valueOf(cleanUp(pCol.html()).replaceAll("[^0-9]", ""));
         } catch (NumberFormatException e) {
             sequence = -1;
         }
 
         return sequence;
     }
 
     private String parseExpiredDate(Element pCol) {
         return cleanUp(pCol.html());
     }
 
     private String parseCompCode(Element pCol) {
          final Elements imgs = pCol.select("img");
          Element img = null;
 
          if (imgs.size() > 0) {
              img = imgs.first();
 
              if (img != null) {
                  final String onClick = img.attr("onclick");
 
                 int compCodeStart = onClick.indexOf("comp_code=");
                 int compCodeEnd = onClick.indexOf("&id=&type");
 
                 if (compCodeStart != -1 && compCodeEnd != -1) {
                     return onClick.substring(compCodeStart + 10, compCodeEnd);
                 }
              }
          }
 
          return null;
     }
 
     private Item parseDataCol(Element pCol) {
         String html = cleanUp(pCol.html());
 
         final Item itemRow = new Item();
         final String[] temps = html.split("\n", 10);
 
         if (temps == null || temps.length == 0) return itemRow;
 
         itemRow.itemName = temps[0];
         switch (mItemType) {
             case PRODUCT:
                 if (temps.length > 2) {
                     itemRow.itemBrand = parseBrand(temps[1]);
                     itemRow.itemCompanyName = parseCompName(temps[2]);
                 }
                 break;
             case PREMISES:
             case MENU:
                 itemRow.itemBrand = null;
                 if (temps.length > 1) {
                     itemRow.itemCompanyName = parseCompName(temps[1]);
                 }
                 break;
         }
 
         return itemRow;
     }
 
     private String parseBrand(String pBrandRow) {
         if (mItemType == ItemType.PRODUCT) {
             final String brand = "BRAND: ";
             final int start = pBrandRow.indexOf(brand);
 
             if (start == -1 || brand.length() <= start) {
                 return "";
             } else {
                 return pBrandRow.substring(7).trim();
             }
 
         }
 
         return "";
     }
 
     private String parseCompName(String pCompNameRow) {
         String compName = "";
 
         if (pCompNameRow.trim().length() == 0) return compName;
 
         switch (mItemType) {
             case PRODUCT:
             case MENU:
                 compName = pCompNameRow.substring(1).trim();
                 break;
             case PREMISES:
                 compName = pCompNameRow.replaceFirst("^\\(", "").replaceFirst("\\)$", "").trim();
                 break;
             default:
                 compName = pCompNameRow.trim();
         }
 
         return compName;
     }
 
     private String cleanUp(String pStr) {
         final Whitelist whiteList = new Whitelist();
         pStr = Jsoup.clean(pStr, whiteList.addTags("br"));
 
         return pStr.replace("<br />", "")
         .replace("<br>", "")
         .replace("&nbsp;", " ")
         .replaceAll("(?m)(?:^|\\G) ", "")
         .replaceAll("r\r?\n", "\n");
     }
 }

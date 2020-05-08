 package com.thoughtworks.hp.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.thoughtworks.hp.CustomWindow;
 import com.thoughtworks.hp.R;
 import com.thoughtworks.hp.adapters.AutoSuggestListAdapter;
 import com.thoughtworks.hp.adapters.BuyListAdapter;
 import com.thoughtworks.hp.datastore.ProductTable;
 import com.thoughtworks.hp.datastore.ShoppingListProductTable;
 import com.thoughtworks.hp.models.Product;
 import com.thoughtworks.hp.models.ShoppingList;
 import com.thoughtworks.hp.models.ShoppingListProduct;
 
 public class AddProductActivity extends CustomWindow implements TextWatcher {
 
     private ProductTable productTable;
     private ShoppingListProductTable shoppingListProductTable;
 
     private AutoSuggestListAdapter autoSuggestAdapter;
     private List<Product> autoSuggestedProductList = new ArrayList<Product>();
 
     private BuyListAdapter buyListAdapter;
     private List<Product> toBuyProductList;
 
     private ListView autoSuggestListView;
     private long shoppingListId;
 
     @SuppressWarnings("unused")
 	private BarcodeScanner barcodeScanner;
 
     private TextView costOfShoppingList;
 
     private HashMap<Product, Integer> productsQuantityIndex;
     private List<Product> products;
     private double cost;
 
 
     @Override
     public void onCreate(Bundle savedInstance) {
         super.onCreate(savedInstance);
         setContentView(R.layout.add_product);
 
         this.shoppingListId = getIntent().getLongExtra(ShoppingList.SHOPPING_LIST_ID, 1);
 
         initDependencies();
         bindBarcodeScanner();
         initToBuyListView();
         initAutoSuggestListView();
         initCostOfShoppingList();
         attachSelfAsTextWatcherToSearchBox();
     }
 
     private void initCostOfShoppingList() {
         costOfShoppingList = (TextView) findViewById(R.id.cost_of_shopping_list);
         costOfShoppingList.setText(String.valueOf(cost));
     }
 
     private void bindBarcodeScanner() {
         barcodeScanner = new BarcodeScanner(this, (Button) this.findViewById(R.id.scan_button));
     }
 
 //    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 //        Product productFromBarcode = barcodeScanner.fetchProductFromBarcodeData(requestCode, resultCode, intent);
 //        if(productFromBarcode != null) {
 //            addAndPersistProductInShoppingList(productFromBarcode);
 //        }
 //    }
 
     private void initDependencies() {
         this.productTable = new ProductTable();
         this.shoppingListProductTable = new ShoppingListProductTable();
     }
 
     private void initToBuyListView() {
         this.toBuyProductList = fetchProductsForShoppingList()!= null? fetchProductsForShoppingList() : new ArrayList<Product>();
         this.productsQuantityIndex = new HashMap<Product, Integer>();
         cost=0;
         for(Product product : toBuyProductList){
             int quantity =  shoppingListProductTable.findQuantityOnProductAndShoppingList(this.shoppingListId, product.getId());
             if(quantity !=-1){
                 productsQuantityIndex.put(product, quantity);
                 cost += Math.round(product.getPrice() * quantity);
             }
         }
         products = new ArrayList<Product>(productsQuantityIndex.keySet());
         this.buyListAdapter = new BuyListAdapter(this, R.layout.product_line_item,  productsQuantityIndex, products);
         ListView toBuyProductListView = (ListView) this.findViewById(R.id.to_buy_product_view);
         toBuyProductListView.setAdapter(this.buyListAdapter);
         toBuyProductListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Product product = toBuyProductList.get(position);
                 Intent intent = new Intent(AddProductActivity.this, AddQuantityActivity.class);
                 intent.putExtra("product_id", product.getId());
                 intent.putExtra("Shopping_list_id", shoppingListId);
                 startActivityForResult(intent, 1);
             }
         });
     }
 
     protected void onActivityResult(int req, int res, Intent data) {
         finish();
         startActivity(this.getIntent());
     }
 
     private List<Product> fetchProductsForShoppingList() {
         return productTable.findByShoppingList(this.shoppingListId);
     }
 
     private void initAutoSuggestListView() {
         this.autoSuggestAdapter = new AutoSuggestListAdapter(this, R.layout.product_auto_suggest_line_item, autoSuggestedProductList);
         this.autoSuggestListView = (ListView) this.findViewById(R.id.auto_suggest_list);
         autoSuggestListView.setAdapter(this.autoSuggestAdapter);
         autoSuggestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Product product = autoSuggestedProductList.get(position);
                 addAndPersistProductInShoppingList(product);
                 Intent intent = new Intent(AddProductActivity.this, AddQuantityActivity.class);
                 intent.putExtra("product_id", product.getId());
                 intent.putExtra("Shopping_list_id", shoppingListId);
                 startActivityForResult(intent, 1);
             }
         });
     }
 
     private void addAndPersistProductInShoppingList(Product product) {
         saveSelectedProductToShoppingList(product);
         addSelectedProductToListing(product);
         resetAutoSuggestList();
     }
 
     private void saveSelectedProductToShoppingList(Product product) {
         ShoppingListProduct newShoppingListProduct = new ShoppingListProduct(product.getId(), this.shoppingListId);
         this.shoppingListProductTable.create(newShoppingListProduct);
     }
 
     private void addSelectedProductToListing(Product product) {
         toBuyProductList.add(product);
         int quantity =  shoppingListProductTable.findQuantityOnProductAndShoppingList(this.shoppingListId, product.getId());
         productsQuantityIndex.put(product, quantity);
         products.add(product);
         cost+=product.getPrice();
         buyListAdapter.notifyDataSetChanged();
     }
 
     private void resetAutoSuggestList() {
         autoSuggestedProductList.clear();
         autoSuggestAdapter.notifyDataSetChanged();
         ((EditText)AddProductActivity.this.findViewById(R.id.searchBox)).setText("");
         autoSuggestListView.setVisibility(View.INVISIBLE);
         initCostOfShoppingList();
     }
 
     private List<Product> findMatchingProducts(String nameFragment) {
         return productTable.findByMatchingName(nameFragment);
     }
 
     private void attachSelfAsTextWatcherToSearchBox() {
         ((EditText)this.findViewById(R.id.searchBox)).addTextChangedListener(this);
     }
 
     @Override
     public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
         if (charSequence == null || charSequence.length() < 1) return;
 
         autoSuggestListView.setVisibility(View.VISIBLE);
         autoSuggestedProductList.clear();
         autoSuggestedProductList.addAll(findMatchingProducts(charSequence.toString()));
         clearAlreadyAddedProductsFrom(autoSuggestedProductList);
         autoSuggestAdapter.notifyDataSetChanged();
     }
 
     private void clearAlreadyAddedProductsFrom(List<Product> autoSuggestedProductList) {
         autoSuggestedProductList.removeAll(this.toBuyProductList);
     }
 
     @Override
     public void afterTextChanged(Editable editable) { }
 
     @Override
     public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) { }
 }

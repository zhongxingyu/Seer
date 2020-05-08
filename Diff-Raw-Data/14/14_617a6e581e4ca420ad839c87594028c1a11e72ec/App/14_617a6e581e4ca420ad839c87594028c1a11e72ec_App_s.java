 /**
  * Copyright (C) 2012 uphy.jp
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package jp.uphy.dsptn.guicesample;
 
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 
 
 /**
  * ハーゲンダッツとピッツァとうまい棒を買うアプリケーションです。
  * 
  * @author Yuhi Ishikura
  */
 public class App {
 
   private Shop shop = Shop.getInstance();
   private CreditCard creditCard;
 
   /**
    * {@link App}オブジェクトを構築します。
    */
   @Inject
  public App(Injector injector) {
    this.creditCard = injector.getInstance(CreditCard.class);
   }
 
   public void buyMyFavorites() {
     shop.buy(Items.HARGEN_DAZS, creditCard);
     shop.buy(Items.PIZZA, creditCard);
     shop.buy(Items.UMAIBO, creditCard);
   }
 
   public CreditCard getCreditCard() {
     return this.creditCard;
   }
 
   public static void main(String[] args) {
     final Injector injector = Guice.createInjector(new CreditCardModule());
     final App app = injector.getInstance(App.class);
     app.buyMyFavorites();
   }
 
 }

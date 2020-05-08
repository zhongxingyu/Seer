 package us.camin;
 
 /*
     This file is part of Caminus
 
     Caminus is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Caminus is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Caminus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.io.IOException;
 import java.util.List;
 import java.util.ArrayList;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
 import us.camin.api.BalanceAdjustResponse;
 
 public class EconomyAPI implements Economy {
     private Plugin m_p;
 
     public EconomyAPI(Plugin p) {
         m_p = p;
     }
 
     @Override
     public boolean isEnabled() {
         return true;
     }
 
     @Override
     public String getName() {
         return "Caminus";
     }
 
     @Override
     public boolean hasBankSupport() {
         return false;
     }
 
     @Override
     public String format(double amount) {
         return amount+" grist";
     }
 
     @Override
     public String currencyNamePlural() {
         return "grist";
     }
 
     @Override
     public String currencyNameSingular() {
         return "grist";
     }
 
     @Override
     public boolean hasAccount(String playerName) {
         return true;
     }
 
     @Override
     public double getBalance(String playerName) {
         try {
             return m_p.api().getBalance(playerName);
         } catch (IOException e) {
             return 0;
         }
     }
 
     @Override
     public boolean has(String playerName, double amount) {
         return getBalance(playerName) >= amount;
     }
 
     private EconomyResponse adjustPlayer(String playerName, double amount) {
         BalanceAdjustResponse resp;
         try {
            resp = m_p.api().adjustBalance(playerName, -amount);
         } catch (IOException e) {
             return new EconomyResponse(0, 0, ResponseType.FAILURE, "Could not contact api.camin.us.");
         }
         if (resp.success) {
             return new EconomyResponse(amount, resp.newBalance, ResponseType.SUCCESS, resp.message);
         } else {
             return new EconomyResponse(0, resp.newBalance, ResponseType.FAILURE, resp.message);
         }
     }
 
     @Override
     public EconomyResponse withdrawPlayer(String playerName, double amount) {
         return adjustPlayer(playerName, amount);
     }
 
     @Override
     public EconomyResponse depositPlayer(String playerName, double amount) {
         return adjustPlayer(playerName, amount);
     }
 
     private static final EconomyResponse NO_IMPL_RESPONSE = new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Not implemented.");
 
     @Override
     public EconomyResponse createBank(String name, String player) {
         return NO_IMPL_RESPONSE;
     }
 
     @Override
     public EconomyResponse deleteBank(String name) {
         return NO_IMPL_RESPONSE;
     }
 
     @Override
     public EconomyResponse bankBalance(String name) {
         return NO_IMPL_RESPONSE;
     }
 
     @Override
     public EconomyResponse bankHas(String name, double amount) {
         return NO_IMPL_RESPONSE;
     }
 
     @Override
     public EconomyResponse bankWithdraw(String name, double amount) {
         return NO_IMPL_RESPONSE;
     }
 
     @Override
     public EconomyResponse bankDeposit(String name, double amount) {
         return NO_IMPL_RESPONSE;
     }
 
     @Override
     public EconomyResponse isBankOwner(String name, String player) {
         return NO_IMPL_RESPONSE;
     }
 
     @Override
     public EconomyResponse isBankMember(String name, String player) {
         return NO_IMPL_RESPONSE;
     }
 
     @Override
     public boolean createPlayerAccount(String name) {
         return true;
     }
 
     @Override
     public List<String> getBanks() {
         return new ArrayList<String>();
     }
 }

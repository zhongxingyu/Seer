 /*
  *  Straight - A system to manage financial demands for small and decentralized
  *  organizations.
  *  Copyright (C) 2011  Octahedron 
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.figgo.modules.bank;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import br.octahedron.cotopaxi.eventbus.Subscriber;
 import br.octahedron.figgo.modules.DomainModuleSpec;
 import br.octahedron.figgo.modules.Module;
 import br.octahedron.figgo.modules.bank.manager.ContractPaidSubscriber;
 import br.octahedron.figgo.modules.domain.data.ModuleConfiguration;
 import br.octahedron.figgo.modules.domain.data.ModuleProperty;
 
 /**
  * @author Danilo Queiroz
  */
 public class BankSpec implements DomainModuleSpec {
 
 	@Override
 	public Type getModuleType() {
 		return Type.DOMAIN;
 	}
 
 	@Override
 	public boolean hasDomainSpecificConfiguration() {
 		return true;
 	}
 
 	@Override
 	public ModuleConfiguration getDomainSpecificModuleConfiguration() {
 		ModuleConfiguration bankConfig = new ModuleConfiguration(Module.BANK.name());
 		bankConfig.addProperty(new ModuleProperty("name", "Banco", "[A-Za-z0-9 _-]{5,}", "O nome do banco."));
 		bankConfig.addProperty(new ModuleProperty("currency", "Dinheiro", "", "O nome da moeda do banco."));
 		bankConfig.addProperty(new ModuleProperty("currencyAbreviation", "$", "", "A abreviação para a moeda do banco."));
 		return bankConfig;
 	}
 
 	@Override
 	public boolean hasSubscribers() {
 		return false;
 	}
 
 	@Override
 	public Set<Class<? extends Subscriber>> getSubscribers() {
 		Set<Class<? extends Subscriber>> subscribers = new HashSet<Class<? extends Subscriber>>();
 		subscribers.add(ContractPaidSubscriber.class);
 		return subscribers;
 	}
 
 	@Override
 	public Set<ActionSpec> getModuleActions() {
 		Set<ActionSpec> actions = new HashSet<ActionSpec>();
 		// BankController actions
 		actions.add(new ActionSpec("IndexBank"));
 		actions.add(new ActionSpec("TransferBank"));
 		actions.add(new ActionSpec("StatementBank"));
 		actions.add(new ActionSpec("StatsBank"));
 		// BankAdminController
 		actions.add(new ActionSpec("ShareBank", true));
 		actions.add(new ActionSpec("BallastBank", true));
 		
 		return actions;
 	}
 }

 /*
  * Copyright (C) 2008 TranceCode Software
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  *
  * $Id$
  */
 package org.trancecode.xproc;
 
 import org.trancecode.annotation.ReturnsNullable;
 import org.trancecode.core.CollectionUtil;
 import org.trancecode.core.ObjectUtil;
 import org.trancecode.xml.Location;
 import org.trancecode.xproc.step.StepFunctions;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.base.Predicates;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 
 import net.sf.saxon.s9api.QName;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author Herve Quiroz
  * @version $Revision$
  */
 public final class Step extends AbstractHasLocation
 {
 	private static final Logger LOG = LoggerFactory.getLogger(Step.class);
 	private static final List<Variable> EMPTY_VARIABLE_LIST = ImmutableList.of();
 	private static final Map<QName, Variable> EMPTY_PARAMETER_MAP = ImmutableMap.of();
 	private static final Map<String, Port> EMPTY_PORT_MAP = Collections.emptyMap();
 	private static final List<Step> EMPTY_STEP_LIST = Collections.emptyList();
 
 	private static final Predicate<Port> PREDICATE_IS_INPUT_PORT = new Predicate<Port>()
 	{
 		public boolean apply(final Port port)
 		{
 			return port.isInput();
 		}
 	};
 
 	private static final Predicate<Port> PREDICATE_IS_OUTPUT_PORT = new Predicate<Port>()
 	{
 		public boolean apply(final Port port)
 		{
 			return port.isOutput();
 		}
 	};
 
 	private static final Predicate<Port> PREDICATE_IS_PARAMETER_PORT = new Predicate<Port>()
 	{
 		public boolean apply(final Port port)
 		{
 			return port.isParameter();
 		}
 	};
 
 	private final Predicate<Port> PREDICATE_IS_XPATH_CONTEXT_PORT = new Predicate<Port>()
 	{
 		public boolean apply(final Port port)
 		{
 			return isXPathContextPort(port);
 		}
 	};
 
 	private final Map<QName, Variable> parameters;
 	private final List<Variable> variables;
 
 	private final Map<String, Port> ports;
 
 	private final QName type;
 	private final String name;
 	private final StepProcessor stepProcessor;
 	private final List<Step> steps;
 	private final boolean compoundStep;
 
 
 	public static Step newStep(final QName type, final StepProcessor stepProcessor, final boolean compoundStep)
 	{
 		return new Step(type, null, null, stepProcessor, compoundStep, EMPTY_VARIABLE_LIST, EMPTY_PARAMETER_MAP,
 			EMPTY_PORT_MAP, EMPTY_STEP_LIST);
 	}
 
 
 	private Step(
 		final QName type, final String name, final Location location, final StepProcessor stepProcessor,
 		final boolean compoundStep, final Iterable<Variable> variables, final Map<QName, Variable> parameters,
 		final Map<String, Port> ports, final Iterable<Step> steps)
 	{
 		super(location);
 
 		this.type = type;
 		this.name = name;
 
 		assert stepProcessor != null;
 		this.stepProcessor = stepProcessor;
 
 		this.compoundStep = compoundStep;
 
 		this.variables = ImmutableList.copyOf(variables);
 		this.parameters = ImmutableMap.copyOf(parameters);
 		this.ports = ImmutableMap.copyOf(ports);
 		this.steps = ImmutableList.copyOf(steps);
 	}
 
 
 	public Step setName(final String name)
 	{
 		LOG.trace("{} -> {}", this.name, name);
 
 		if (ObjectUtil.equals(this.name, name))
 		{
 			return this;
 		}
 
 		Step step = new Step(type, name, location, stepProcessor, compoundStep, variables, parameters, ports, steps);
 		for (final Port port : ports.values())
 		{
 			step = step.withPort(port.setStepName(name));
 		}
 
 		return step;
 	}
 
 
 	public boolean isCompoundStep()
 	{
 		return compoundStep;
 	}
 
 
 	public Step declareVariable(final Variable variable)
 	{
 		assert !Variables.containsVariable(variables, variable.getName()) : "step = " + name + " ; variable = "
 			+ variable.getName() + " ; variables = " + variables;
 		return new Step(type, name, location, stepProcessor, compoundStep, CollectionUtil.append(variables, variable),
 			parameters, ports, steps);
 	}
 
 
 	public Step declareVariables(final Iterable<Variable> variables)
 	{
 		return CollectionUtil.apply(this, variables, StepFunctions.declareVariable());
 	}
 
 
 	public String getName()
 	{
 		return name;
 	}
 
 
 	public final Step declarePort(final Port port)
 	{
 		LOG.trace("port = {}", port);
 
 		return declarePorts(Collections.singleton(port));
 	}
 
 
 	public final Step declarePorts(final Iterable<Port> ports)
 	{
 		LOG.trace("ports = {}", ports);
 
 		final Map<String, Port> newPorts = Maps.newHashMap(this.ports);
 		newPorts.putAll(Maps.uniqueIndex(ports, Port.GET_PORT_NAME_FUNCTION));
 
 		return new Step(type, name, location, stepProcessor, compoundStep, variables, parameters, newPorts, steps);
 	}
 
 
 	public Port getPort(final String name)
 	{
 		assert ports.containsKey(name) : "step = " + getName() + " ; port = " + name + " ; ports = " + ports.keySet();
 		return ports.get(name);
 	}
 
 
 	public Map<String, Port> getPorts()
 	{
 		return ports;
 	}
 
 
 	private boolean isXPathContextPort(final Port port)
 	{
		if (port.isInput() && !port.isParameter())
 		{
 			if (port.getPortName().equals(XProcPorts.XPATH_CONTEXT))
 			{
 				return true;
 			}
 
 			if (isPrimary(port))
 			{
 				return !ports.containsKey(XProcPorts.XPATH_CONTEXT);
 			}
 		}
 
 		return false;
 	}
 
 
 	public Environment run(final Environment environment)
 	{
 		LOG.trace("name = {} ; type = {}", name, type);
 		return stepProcessor.run(this, environment);
 	}
 
 
 	@ReturnsNullable
 	public Port getPrimaryInputPort()
 	{
 		final List<Port> inputPorts = ImmutableList.copyOf(getInputPorts());
 		LOG.trace("inputPorts = {}", inputPorts);
 		if (inputPorts.size() == 1)
 		{
 			final Port inputPort = Iterables.getOnlyElement(inputPorts);
 			if (!inputPort.isNotPrimary())
 			{
 				return inputPort;
 			}
 		}
 
 		for (final Port inputPort : inputPorts)
 		{
 			if (inputPort.isPrimary())
 			{
 				return inputPort;
 			}
 		}
 
 		return null;
 	}
 
 
 	@ReturnsNullable
 	public Port getPrimaryParameterPort()
 	{
 		final List<Port> parameterPorts = ImmutableList.copyOf(getParameterPorts());
 		LOG.trace("parameterPorts = {}", parameterPorts);
 		if (parameterPorts.size() == 1)
 		{
 			final Port parameterPort = Iterables.getOnlyElement(parameterPorts);
 			if (!parameterPort.isNotPrimary())
 			{
 				return parameterPort;
 			}
 		}
 
 		for (final Port parameterPort : parameterPorts)
 		{
 			if (parameterPort.isPrimary())
 			{
 				return parameterPort;
 			}
 		}
 
 		return null;
 	}
 
 
 	@ReturnsNullable
 	public Port getPrimaryOutputPort()
 	{
 		final List<Port> outputPorts = ImmutableList.copyOf(getOutputPorts());
 		LOG.trace("outputPorts = {}", outputPorts);
 		if (outputPorts.size() == 1)
 		{
 			final Port outputPort = Iterables.getOnlyElement(outputPorts);
 			if (!outputPort.isNotPrimary())
 			{
 				return outputPort;
 			}
 		}
 
 		for (final Port outputPort : outputPorts)
 		{
 			if (outputPort.isPrimary())
 			{
 				return outputPort;
 			}
 		}
 
 		return null;
 	}
 
 
 	private boolean isPrimary(final Port port)
 	{
 		if (port.isParameter())
 		{
 			return isPrimary(port, getParameterPorts());
 		}
 
 		if (port.isInput())
 		{
 			return isPrimary(port, getInputPorts());
 		}
 
 		assert port.isOutput();
 		return isPrimary(port, getOutputPorts());
 	}
 
 
 	private static boolean isPrimary(final Port port, final Iterable<Port> ports)
 	{
 		assert port != null;
 
 		if (port.isNotPrimary())
 		{
 			return false;
 		}
 
 		if (port.isPrimary())
 		{
 			return true;
 		}
 
 		if (Iterables.size(ports) == 1)
 		{
 			return true;
 		}
 
 		return false;
 	}
 
 
 	public Iterable<Port> getInputPorts()
 	{
 		return Iterables.filter(ports.values(), PREDICATE_IS_INPUT_PORT);
 	}
 
 
 	public Iterable<Port> getOutputPorts()
 	{
 		return Iterables.filter(ports.values(), PREDICATE_IS_OUTPUT_PORT);
 	}
 
 
 	public Iterable<Port> getParameterPorts()
 	{
 		return Iterables.filter(ports.values(), PREDICATE_IS_PARAMETER_PORT);
 	}
 
 
 	public Step withOption(final QName name, final String select)
 	{
 		assert Variables.containsVariable(variables, name);
 
 		final Iterable<Variable> newVariables = Iterables.transform(variables, new Function<Variable, Variable>()
 		{
 			@Override
 			public Variable apply(final Variable variable)
 			{
 				if (variable.getName().equals(name))
 				{
 					assert variable.isOption();
 					return variable.setSelect(select);
 				}
 
 				return variable;
 			}
 		});
 
 		return new Step(type, this.name, location, stepProcessor, compoundStep, newVariables, parameters, ports, steps);
 	}
 
 
 	public Step withParam(final QName name, final String select, final String value, final Location location)
 	{
 		assert !parameters.containsKey(name);
 
 		final Iterable<Variable> newVariables =
 			Variables.setOrAddVariable(variables, Variable.newParameter(name, location).setSelect(select).setValue(
 				value));
 
 		return new Step(type, this.name, location, stepProcessor, compoundStep, newVariables, parameters, ports, steps);
 	}
 
 
 	public Step withOptionValue(final QName name, final String value)
 	{
 		assert Variables.containsVariable(variables, name);
 
 		final Iterable<Variable> newVariables = Iterables.transform(variables, new Function<Variable, Variable>()
 		{
 			@Override
 			public Variable apply(final Variable variable)
 			{
 				if (variable.getName().equals(name))
 				{
 					assert variable.isOption();
 					return variable.setValue(value);
 				}
 
 				return variable;
 			}
 		});
 
 		return new Step(type, this.name, location, stepProcessor, compoundStep, newVariables, parameters, ports, steps);
 	}
 
 
 	public boolean hasOptionDeclared(final QName name)
 	{
 		return Iterables
 			.any(variables, Predicates.and(VariablePredicates.isNamed(name), VariablePredicates.isOption()));
 	}
 
 
 	@Override
 	public String toString()
 	{
 		return String.format("%s ; name = %s ; ports = %s ; variables = %s", type, name, ports, variables);
 	}
 
 
 	public Step setPortBindings(final String portName, final PortBinding... portBindings)
 	{
 		return withPort(getPort(portName).setPortBindings(portBindings));
 	}
 
 
 	public Step setPortBindings(final String portName, final Iterable<PortBinding> portBindings)
 	{
 		return withPort(getPort(portName).setPortBindings(portBindings));
 	}
 
 
 	public Step withPort(final Port port)
 	{
 		assert ports.containsKey(port.getPortName());
 
 		return new Step(type, name, location, stepProcessor, compoundStep, variables, parameters, CollectionUtil
 			.copyAndPut(ports, port.getPortName(), port), steps);
 	}
 
 
 	@ReturnsNullable
 	public Port getXPathContextPort()
 	{
 		final Port xpathContextPort =
 			Iterables.getOnlyElement(Iterables.filter(getInputPorts(), PREDICATE_IS_XPATH_CONTEXT_PORT), null);
 		LOG.trace("XPath context port = {}", xpathContextPort);
 		return xpathContextPort;
 	}
 
 
 	public Iterable<Variable> getVariables()
 	{
 		return variables;
 	}
 
 
 	public QName getType()
 	{
 		return type;
 	}
 
 
 	public Step setSubpipeline(final Iterable<Step> steps)
 	{
 		assert steps != null;
 		if (ObjectUtil.equals(this.steps, steps))
 		{
 			return this;
 		}
 
 		LOG.trace("steps = {}", steps);
 		return new Step(type, name, location, stepProcessor, compoundStep, variables, parameters, ports, steps);
 	}
 
 
 	public List<Step> getSubpipeline()
 	{
 		return steps;
 	}
 
 
 	public Step setLocation(final Location location)
 	{
 		if (ObjectUtil.equals(this.location, location))
 		{
 			return this;
 		}
 
 		return new Step(type, name, location, stepProcessor, compoundStep, variables, parameters, ports, steps);
 	}
 
 
 	public Variable getVariable(final QName name)
 	{
 		return Variables.getVariable(variables, name);
 	}
 }

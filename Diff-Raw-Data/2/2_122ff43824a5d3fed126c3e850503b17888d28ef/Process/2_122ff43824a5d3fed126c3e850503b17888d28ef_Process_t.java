 package uk.org.smithfamily.utils.normaliser;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 
 import org.apache.commons.lang3.StringUtils;
 
 import uk.org.smithfamily.mslogger.ecuDef.Constant;
 import uk.org.smithfamily.utils.normaliser.curveeditor.ColumnLabel;
 import uk.org.smithfamily.utils.normaliser.curveeditor.CurveDefinition;
 import uk.org.smithfamily.utils.normaliser.curveeditor.CurveTracker;
 import uk.org.smithfamily.utils.normaliser.curveeditor.LineLabel;
 import uk.org.smithfamily.utils.normaliser.curveeditor.XAxis;
 import uk.org.smithfamily.utils.normaliser.tableeditor.GridHeight;
 import uk.org.smithfamily.utils.normaliser.tableeditor.GridOrient;
 import uk.org.smithfamily.utils.normaliser.tableeditor.PreProcessor;
 import uk.org.smithfamily.utils.normaliser.tableeditor.TableDefinition;
 import uk.org.smithfamily.utils.normaliser.tableeditor.TableTracker;
 import uk.org.smithfamily.utils.normaliser.tableeditor.UpDownLabel;
 import uk.org.smithfamily.utils.normaliser.tableeditor.XBins;
 import uk.org.smithfamily.utils.normaliser.tableeditor.YBins;
 import uk.org.smithfamily.utils.normaliser.tableeditor.ZBins;
 
 public class Process
 {
 	private static String deBinary(String group)
 	{
 		Matcher binNumber = Patterns.binary.matcher(group);
 		if (!binNumber.matches())
 		{
 			return group;
 		} 
 		else
 		{
 			String binNum = binNumber.group(2);
 			int num = Integer.parseInt(binNum, 2);
 			String expr = binNumber.group(1) + num + binNumber.group(3);
 			return deBinary(expr);
 		}
 	}
 
 	private static String removeComments(String line)
 	{
 		line += "; junk";
 		line = StringUtils.trim(line).split(";")[0];
 		line = line.trim();
 		return line;
 	}
 
 	private static String convertC2JavaBoolean(String expression)
 	{
 		Matcher matcher = Patterns.booleanConvert.matcher(expression);
 		StringBuffer result = new StringBuffer(expression.length());
 		while (matcher.find())
 		{
 			matcher.appendReplacement(result, "");
 			result.append(matcher.group(1) + " ? 1 : 0)");
 		}
 		matcher.appendTail(result);
 		expression = result.toString();
 		return expression;
 
 	}
 
 	private static boolean isFloatingExpression(ECUData ecuData,
 			String expression)
 	{
 		boolean result = expression.contains(".");
 		if (result)
 		{
 			return result;
 		}
 		for (String var : ecuData.getRuntimeVars().keySet())
 		{
 			if (expression.contains(var)
 					&& ecuData.getRuntimeVars().get(var).equals("double"))
 			{
 				result = true;
 			}
 		}
 		for (String var : ecuData.getEvalVars().keySet())
 		{
 			if (expression.contains(var)
 					&& ecuData.getEvalVars().get(var).equals("double"))
 			{
 				result = true;
 			}
 		}
 
 		return result;
 	}
 
 	static void processExpr(ECUData ecuData, String line)
 	{
 		String definition = null;
 		line = removeComments(line);
 
 		line = StringUtils.replace(line, "timeNow", "timeNow()");
 		Matcher bitsM = Patterns.bits.matcher(line);
 		Matcher scalarM = Patterns.scalar.matcher(line);
 		Matcher exprM = Patterns.expr.matcher(line);
 		Matcher ochGetCommandM = Patterns.ochGetCommand.matcher(line);
 		Matcher ochBlockSizeM = Patterns.ochBlockSize.matcher(line);
 		if (bitsM.matches())
 		{
 			String name = bitsM.group(1);
 			String offset = bitsM.group(3);
 			String start = bitsM.group(4);
 			String end = bitsM.group(5);
 			String ofs = "0";
 			if (end.contains("+"))
 			{
 				String[] parts = end.split("\\+");
 				end = parts[0];
 				ofs = parts[1];
 			}
 			definition = (name + " = MSUtils.getBits(ochBuffer," + offset + ","
 					+ start + "," + end + "," + ofs + ");");
 			ecuData.getRuntime().add(definition);
 			ecuData.getRuntimeVars().put(name, "int");
 		}
 		else if (scalarM.matches())
 		{
 			String name = scalarM.group(1);
 			if (constantDefined(ecuData, name))
 			{
 				name += "RT";
 			}
 			String dataType = scalarM.group(2);
 			String offset = scalarM.group(3);
 			String scalingRaw = scalarM.group(4);
 			String[] scaling = scalingRaw.split(",");
 			String scale = scaling[1].trim();
 			String numOffset = scaling[2].trim();
 			if (Double.parseDouble(scale) != 1)
 			{
 				ecuData.getRuntimeVars().put(name, "double");
 			} else
 			{
 				ecuData.getRuntimeVars().put(name, "int");
 			}
 			definition = Output.getScalar("ochBuffer", ecuData.getRuntimeVars()
 					.get(name), name, dataType, offset, scale, numOffset);
 			ecuData.setFingerprintSource(ecuData.getFingerprintSource()
 					+ definition);
 			ecuData.getRuntime().add(definition);
 		}
 		else if (exprM.matches())
 		{
 			String name = exprM.group(1);
 			if ("pwma_load".equals(name))
 			{
 				// Hook to hang a break point on
 				@SuppressWarnings("unused")
 				int x = 1;
 			}
 			String expression = deBinary(exprM.group(2).trim());
 			Matcher ternaryM = Patterns.ternary.matcher(expression);
 			if (ternaryM.matches())
 			{
 				// System.out.println("BEFORE : " + expression);
 				String test = ternaryM.group(1);
 				String values = ternaryM.group(2);
 				if (StringUtils.containsAny(test, "<>!="))
 				{
 					expression = "(" + test + ") ? " + values;
 				} else
 				{
 					expression = "((" + test + ") != 0 ) ? " + values;
 				}
 				// System.out.println("AFTER  : " + expression + "\n");
 			}
 			if (expression.contains("*") && expression.contains("=="))
 			{
 				expression = convertC2JavaBoolean(expression);
 			}
 			definition = name + " = (" + expression + ");";
 
 			// If the expression contains a division, wrap it in a try/catch to
 			// avoid division by zero
 			if (expression.contains("/"))
 			{
 				definition = "try\n" + Output.TAB + Output.TAB + "{\n"
 						+ Output.TAB + Output.TAB + Output.TAB + definition
 						+ "\n" + Output.TAB + Output.TAB + "}\n" + Output.TAB
 						+ Output.TAB + "catch (ArithmeticException e) {\n"
 						+ Output.TAB + Output.TAB + Output.TAB + name
 						+ " = 0;\n" + Output.TAB + Output.TAB + "}";
 			}
 
 			ecuData.getRuntime().add(definition);
 			if (isFloatingExpression(ecuData, expression))
 			{
 				ecuData.getEvalVars().put(name, "double");
 			}
 			else
 			{
 				ecuData.getEvalVars().put(name, "int");
 			}
 		}
 		else if (ochGetCommandM.matches())
 		{
 			String och = ochGetCommandM.group(1);
 			if (och.length() > 1)
 			{
 				och = Output.HexStringToBytes(ecuData, och, 0, 0, 0);
 			} else
 			{
 				och = "'" + och + "'";
 			}
 			ecuData.setOchGetCommandStr("byte [] ochGetCommand = new byte[]{"
 					+ och + "};");
 		}
 		else if (ochBlockSizeM.matches())
 		{
 			ecuData.setOchBlockSizeStr("int ochBlockSize = "
 					+ ochBlockSizeM.group(1) + ";");
 		}
 		else if (line.startsWith("#"))
 		{
 			ecuData.getRuntime().add(processPreprocessor(ecuData, line));
 		}
 		else if (!StringUtils.isEmpty(line))
 		{
 			System.out.println(line);
 		}
 	}
 
 	/**
 	 * Occasionally we get a collision between the name of a constant and an
 	 * expression. Test for that here.
 	 * 
 	 * @param ecuData
 	 * @param name
 	 * @return
 	 */
 	private static boolean constantDefined(ECUData ecuData, String name)
 	{
 		for (Constant c : ecuData.getConstants())
 		{
 			if (c.getName().equals(name))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	static void processLogEntry(ECUData ecuData, String line)
 	{
 		line = removeComments(line);
 
 		Matcher logM = Patterns.log.matcher(line);
 		if (logM.matches())
 		{
 			String header = logM.group(2);
 			String variable = logM.group(1);
 			if ("double".equals(ecuData.getRuntimeVars().get(variable)))
 			{
 				variable = "round(" + variable + ")";
 			}
 			ecuData.getLogHeader().add(
 					"b.append(\"" + header + "\").append(\"\\t\");");
 			ecuData.getLogRecord().add(
 					"b.append(" + variable + ").append(\"\\t\");");
 		} 
 		else if (line.startsWith("#"))
 		{
 			String directive = processPreprocessor(ecuData, line);
 			ecuData.getLogHeader().add(directive);
 			ecuData.getLogRecord().add(directive);
 		}
 	}
 
 	static String processPreprocessor(ECUData ecuData, String line)
 	{
 		String filtered;
 		boolean stripped = false;
 
 		filtered = line.replace("  ", " ");
 		stripped = filtered.equals(line);
 		while (!stripped)
 		{
 			line = filtered;
 			filtered = line.replace("  ", " ");
 			stripped = filtered.equals(line);
 		}
 		String[] components = line.split(" ");
 		String flagName = components.length > 1 ? sanitize(components[1]) : "";
 		if (components[0].equals("#if"))
 		{
 			ecuData.getFlags().add(flagName);
 			return ("if (" + flagName + ")\n        {");
 		}
 		if (components[0].equals("#elif"))
 		{
 			ecuData.getFlags().add(flagName);
 			return ("}\n        else if (" + flagName + ")\n        {");
 		}
 		if (components[0].equals("#else"))
 		{
 			return ("}\n        else\n        {");
 		}
 		if (components[0].equals("#endif"))
 		{
 			return ("}");
 		}
 
 		return "";
 	}
 
 	private static String sanitize(String flagName)
 	{
 		return StringUtils.replace(flagName, "!", "n");
 	}
 
 	static void processFrontPage(ECUData ecuData, String line)
 	{
 		line = removeComments(line);
 
 		Matcher dgM = Patterns.defaultGauge.matcher(line);
 		if (dgM.matches())
 		{
 			ecuData.getDefaultGauges().add(dgM.group(1));
 		}
 	}
 
 	static void processHeader(ECUData ecuData, String line)
 	{
 		Matcher queryM = Patterns.queryCommand.matcher(line);
 		if (queryM.matches())
 		{
 			ecuData.setQueryCommandStr("byte[] queryCommand = new byte[]{'"
 					+ queryM.group(1) + "'};");
 			return;
 		}
 
 		Matcher sigM = Patterns.signature.matcher(line);
 		Matcher sigByteM = Patterns.byteSignature.matcher(line);
 		if (sigM.matches())
 		{
 			String tmpsig = sigM.group(1);
 			if (line.contains("null"))
 			{
 				tmpsig += "\\0";
 			}
 			ecuData.setClassSignature("\"" + tmpsig + "\"");
 			ecuData.setSignatureDeclaration("String signature = \"" + tmpsig
 					+ "\";");
 		} else if (sigByteM.matches())
 		{
 			String b = sigByteM.group(1).trim();
 			ecuData.setClassSignature("new String(new byte[]{" + b + "})");
 			ecuData.setSignatureDeclaration("String signature = \"\"+(byte)"
 					+ b + ";");
 		}
 
 	}
 
 	static void processGaugeEntry(ECUData ecuData, String line)
 	{
 		line = removeComments(line);
 
 		Matcher m = Patterns.gauge.matcher(line);
 		if (m.matches())
 		{
 			String name = m.group(1);
 			String channel = m.group(2);
 			String title = m.group(3);
 			String units = m.group(4);
 			String lo = m.group(5);
 			String hi = m.group(6);
 			String loD = m.group(7);
 			String loW = m.group(8);
 			String hiW = m.group(9);
 			String hiD = m.group(10);
 			String vd = m.group(11);
 			String ld = m.group(12);
 
 			String g = String
 					.format("GaugeRegister.INSTANCE.addGauge(new GaugeDetails(\"Gauge\",\"\",\"%s\",\"%s\",%s,\"%s\",\"%s\",%s,%s,%s,%s,%s,%s,%s,%s,45));",
 							name, channel, channel, title, units, lo, hi, loD,
 							loW, hiW, hiD, vd, ld);
 
 			g = g.replace("{", "").replace("}", "");
 			String gd = String
 					.format("<tr><td>Gauge</td><td></td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
 							name, channel, title, units, lo, hi, loD, loW, hiW,
 							hiD, vd, ld);
 			ecuData.getGaugeDoc().add(gd);
 			ecuData.getGaugeDef().add(g);
 		} else if (line.startsWith("#"))
 		{
 			ecuData.getGaugeDef().add(processPreprocessor(ecuData, line));
 			ecuData.getGaugeDoc()
 					.add(String
 							.format("<tr><td colspan=\"12\" id=\"preprocessor\">%s</td></tr>",
 									line));
 		}
 
 	}
 
 	/**
 	 * Process the [Constants] section of the ini file
 	 * 
 	 * @param line
 	 */
 	static void processConstants(ECUData ecuData, String line)
 	{
 		line = removeComments(line);
 		if (StringUtils.isEmpty(line))
 		{
 			return;
 		}
 
		if (line.contains("DI_rpm"))
 		{
 			int x=1;
 		}
 		if (line.contains("messageEnvelopeFormat"))
 		{
 			ecuData.setCRC32Protocol(line.contains("msEnvelope_1.0"));
 		}
 		Matcher pageM = Patterns.page.matcher(line);
 		if (pageM.matches())
 		{
 			ecuData.setCurrentPage(Integer.parseInt(pageM.group(1).trim()));
 			return;
 		}
 		Matcher pageSizesM = Patterns.pageSize.matcher(line);
 		if (pageSizesM.matches())
 		{
 			String values = StringUtils.remove(pageSizesM.group(1), ' ');
 			String[] list = values.split(",");
 			ecuData.setPageSizes(new ArrayList<String>(Arrays.asList(list)));
 		}
 		Matcher pageIdentifersM = Patterns.pageIdentifier.matcher(line);
 		if (pageIdentifersM.matches())
 		{
 			String values = StringUtils.remove(pageIdentifersM.group(1), ' ');
 			values = StringUtils.remove(values, '"');
 			String[] list = values.split(",");
 			ecuData.setPageIdentifiers(new ArrayList<String>(Arrays
 					.asList(list)));
 		}
 
 		Matcher pageActivateM = Patterns.pageActivate.matcher(line);
 		if (pageActivateM.matches())
 		{
 			String values = StringUtils.remove(pageActivateM.group(1), ' ');
 			values = StringUtils.remove(values, '"');
 			String[] list = values.split(",");
 			ecuData.setPageActivateCommands(new ArrayList<String>(Arrays
 					.asList(list)));
 		}
 
 		Matcher pageReadCommandM = Patterns.pageReadCommand.matcher(line);
 		if (pageReadCommandM.matches())
 		{
 			String values = StringUtils.remove(pageReadCommandM.group(1), ' ');
 			values = StringUtils.remove(values, '"');
 			String[] list = values.split(",");
 			ecuData.setPageReadCommands(new ArrayList<String>(Arrays.asList(list)));
 		}
 
 		Matcher interWriteDelayM = Patterns.interWriteDelay.matcher(line);
 		if (interWriteDelayM.matches())
 		{
 			ecuData.setInterWriteDelay(Integer.parseInt(interWriteDelayM.group(1).trim()));
 			return;
 		}
 		Matcher pageActivationDelayM = Patterns.pageActivationDelay
 				.matcher(line);
 		if (pageActivationDelayM.matches())
 		{
 			ecuData.setPageActivationDelayVal(Integer
 					.parseInt(pageActivationDelayM.group(1).trim()));
 			return;
 		}
 		//To allow for MS2GS27
 		line=removeCurlyBrackets(line);
 		Matcher bitsM = Patterns.bits.matcher(line);
 		Matcher constantM = Patterns.constantScalar.matcher(line);
 		Matcher constantSimpleM = Patterns.constantSimple.matcher(line);
 		Matcher constantArrayM = Patterns.constantArray.matcher(line);
 		if (constantM.matches())
 		{
 			String name = constantM.group(1);
 			String classtype = constantM.group(2);
 			String type = constantM.group(3);
 			int offset = Integer.parseInt(constantM.group(4).trim());
 			String units = constantM.group(5);
 			String scaleText = constantM.group(6);
 			String translateText = constantM.group(7);
 			String lowText = constantM.group(8);
 			String highText = constantM.group(9);
 			String digitsText = constantM.group(10);
 			double scale = !StringUtils.isEmpty(scaleText) ? Double
 					.parseDouble(scaleText) : 0;
 			double translate = !StringUtils.isEmpty(translateText) ? Double
 					.parseDouble(translateText) : 0;
 			
 			int digits = !StringUtils.isEmpty(digitsText) ? (int) Double
 					.parseDouble(digitsText) : 0;
 
 			if (!ecuData.getConstants().contains(name))
 			{
 				Constant c = new Constant(ecuData.getCurrentPage(), name,
 						classtype, type, offset, "", units, scale, translate,
 						lowText, highText, digits);
 
 				if (scale == 1.0)
 				{
 					ecuData.getConstantVars().put(name, "int");
 				}
 				else
 				{
 					ecuData.getConstantVars().put(name, "double");
 				}
 				ecuData.getConstants().add(c);
 			}
 		}
 		else if (constantArrayM.matches())
 		{
 
 			String name = constantArrayM.group(1);
 			String classtype = constantArrayM.group(2);
 			String type = constantArrayM.group(3);
 			int offset = Integer.parseInt(constantArrayM.group(4).trim());
 			String shape = constantArrayM.group(5);
 			String units = constantArrayM.group(6);
 			String scaleText = constantArrayM.group(7);
 			String translateText = constantArrayM.group(8);
 			String lowText = constantArrayM.group(9);
 			String highText = constantArrayM.group(10);
 			String digitsText = constantArrayM.group(11);
 			highText = highText.replace("{", "").replace("}", "");
 			double scale = !StringUtils.isEmpty(scaleText) ? Double
 					.parseDouble(scaleText) : 0;
 			double translate = !StringUtils.isEmpty(translateText) ? Double
 					.parseDouble(translateText) : 0;
 			
 			int digits = !StringUtils.isEmpty(digitsText) ? (int) Double
 					.parseDouble(digitsText) : 0;
 
 			if (!ecuData.getConstants().contains(name))
 			{
 				Constant c = new Constant(ecuData.getCurrentPage(), name,
 						classtype, type, offset, shape, units, scale,
 						translate, lowText, highText, digits);
 				if (shape.contains("x"))
 				{
 					ecuData.getConstantVars().put(name, "double[][]");
 				}
 				else
 				{
 					ecuData.getConstantVars().put(name, "double[]");
 				}
 				ecuData.getConstants().add(c);
 			}
 		}
 		else if (constantSimpleM.matches())
 		{
 			String name = constantSimpleM.group(1);
 			String classtype = constantSimpleM.group(2);
 			String type = constantSimpleM.group(3);
 			int offset = Integer.parseInt(constantSimpleM.group(4).trim());
 			String units = constantSimpleM.group(5);
 			double scale = Double.parseDouble(constantSimpleM.group(6));
 			double translate = Double.parseDouble(constantSimpleM.group(7));
 
 			Constant c = new Constant(ecuData.getCurrentPage(), name,
 					classtype, type, offset, "", units, scale, translate, "0", "0",
 					0);
 
 			if (scale == 1.0)
 			{
 				ecuData.getConstantVars().put(name, "int");
 			} else
 			{
 				ecuData.getConstantVars().put(name, "double");
 			}
 			ecuData.getConstants().add(c);
 		}
 		else if (bitsM.matches())
 		{
 			String name = bitsM.group(1);
 			String offset = bitsM.group(3);
 			String start = bitsM.group(4);
 			String end = bitsM.group(5);
 
 			Constant c = new Constant(ecuData.getCurrentPage(), name, "bits",
 					"", Integer.parseInt(offset.trim()), "[" + start + ":"
 							+ end + "]", "", 1, 0, "0", "0", 0);
 			ecuData.getConstantVars().put(name, "int");
 			ecuData.getConstants().add(c);
 
 		}
 		else if (line.startsWith("#"))
 		{
 			String preproc = (processPreprocessor(ecuData, line));
 			Constant c = new Constant(ecuData.getCurrentPage(), preproc, "",
 					"PREPROC", 0, "", "", 0, 0, "0", "0", 0);
 			ecuData.getConstants().add(c);
 		}
 	}
 
 	private static String removeCurlyBrackets(String line)
 	{
 		return line.replaceAll("\\{", "").replaceAll("\\}", "");
 	}
 
 	/**
 	 * Process the [Menu] section of the ini file
 	 * 
 	 * @param line
 	 */
 	static void processMenu(ECUData ecuData, String line)
 	{
 		line = removeComments(line);
 		if (StringUtils.isEmpty(line))
 		{
 			return;
 		}
 
 		Matcher menuDialog = Patterns.menuDialog.matcher(line);
 		Matcher menu = Patterns.menu.matcher(line);
 		Matcher subMenu = Patterns.subMenu.matcher(line);
 
 		if (menuDialog.matches())
 		{
 			// System.out.println("menuDialog Name: " + menuDialog.group(1));
 		}
 		else if (menu.matches())
 		{
 			// System.out.println("menu Label: " + menu.group(1));
 		}
 		else if (subMenu.matches())
 		{
 			// System.out.println("subMenu Name: " + subMenu.group(1));
 			// System.out.println("subMenu Label: " + subMenu.group(3));
 			// System.out.println("subMenu RandomNumber: " + subMenu.group(5));
 			// System.out.println("subMenu Expression: " + subMenu.group(7));
 		}
 	}
 
 	/**
 	 * Process the [TablEditor] section of the ini file
 	 * 
 	 * @param line
 	 */
 	static void processTableEditor(ECUData ecuData, String line)
 	{
 		line = removeComments(line);
 		if (StringUtils.isEmpty(line))
 		{
 			return;
 		}
 
 		Matcher table = Patterns.tablEditorTable.matcher(line);
 		Matcher xBins = Patterns.tablEditorXBins.matcher(line);
 		Matcher yBins = Patterns.tablEditorYBins.matcher(line);
 		Matcher zBins = Patterns.tablEditorZBins.matcher(line);
 		Matcher upDownLabel = Patterns.tablEditorUpDownLabel.matcher(line);
 		Matcher gridHeight = Patterns.tablEditorGridHeight.matcher(line);
 		Matcher gridOrient = Patterns.tablEditorGridOrient.matcher(line);
 
 		final List<TableTracker> tableDefs = ecuData.getTableDefs();
 		TableTracker t = null;
 		if (tableDefs.size() > 0)
 		{
 			t = tableDefs.get(tableDefs.size() - 1);
 		}
         if (t == null)
         {
             t = new TableTracker();
             tableDefs.add(t);
         }
         if (table.matches())
         {
             if (t.isDefinitionCompleted())
             {
                 t = new TableTracker();
                 tableDefs.add(t);
             }
             TableDefinition td = new TableDefinition(t, table.group(1), table.group(2), table.group(3), table.group(4));
             t.addItem(td);
         }
         else if (xBins.matches())
         {
             XBins x = new XBins(xBins.group(1), xBins.group(2), xBins.group(4));
             t.addItem(x);
         }
         else if (yBins.matches())
         {
             YBins y = new YBins(yBins.group(1), yBins.group(2), yBins.group(4));
             t.addItem(y);
         }
         else if (zBins.matches())
         {
             ZBins z = new ZBins(zBins.group(1));
             t.addItem(z);
         }
         else if (upDownLabel.matches())
         {
             UpDownLabel l = new UpDownLabel(upDownLabel.group(1), upDownLabel.group(2));
             t.addItem(l);
         }
         else if (gridHeight.matches())
         {
             GridHeight g = new GridHeight(gridHeight.group(1));
             t.addItem(g);
         }
         else if (gridOrient.matches())
         {
             GridOrient g = new GridOrient(gridOrient.group(1), gridOrient.group(2), gridOrient.group(3));
             t.addItem(g);
         }
         else
         {
             PreProcessor p = new PreProcessor(processPreprocessor(ecuData, line));
             if (t != null && !t.isDefinitionCompleted())
             {
                 t.addItem(p);
             }
             else
             {
                 t = new TableTracker();
                 tableDefs.add(t);
                 t.addItem(p);
             }
         }
 	}
 
 	/**
 	 * Process the [CurveEditor] section of the ini file
 	 * 
 	 * @param line
 	 */
 	static void processCurveEditor(ECUData ecuData, String line)
 	{
 		line = removeComments(line);
 		if (StringUtils.isEmpty(line))
 		{
 			return;
 		}
 
 		line=removeCurlyBrackets(line);
         
 		Matcher curve = Patterns.curve.matcher(line);
 		Matcher columnLabel = Patterns.curveColumnLabel.matcher(line);
 		Matcher xAxis = Patterns.curveXAxis.matcher(line);
 		Matcher yAxis = Patterns.curveYAxis.matcher(line);
 		Matcher xBins = Patterns.curveXBins.matcher(line);
 		Matcher yBins = Patterns.curveYBins.matcher(line);
 		Matcher gauge = Patterns.curveGauge.matcher(line);
 		Matcher lineLabel = Patterns.curveLineLabel.matcher(line);
 
 		final List<CurveTracker> curveDefs = ecuData.getCurveDefs();
         CurveTracker c = null;
         if(line.contains("cltlowlim"))
         {
         	int x =1;
         }
         if (curveDefs.size() > 0)
         {
             c = curveDefs.get(curveDefs.size() - 1);
         }
         if (c == null)
         {
             c = new CurveTracker();
             curveDefs.add(c);
         }
         
 		if (curve.matches())
 		{
 		    if (c.isDefinitionCompleted())
             {
                 c = new CurveTracker();
                 curveDefs.add(c);
             }
 		    CurveDefinition cd = new CurveDefinition(c, curve.group(1), curve.group(2));
             c.addItem(cd);
 		}
 		else if (columnLabel.matches())
 		{
             ColumnLabel x = new ColumnLabel(columnLabel.group(1), columnLabel.group(2));
             c.addItem(x);
 		}
 		else if (xAxis.matches())
 		{
 		    XAxis x = new XAxis(xAxis.group(1), xAxis.group(2), xAxis.group(3));
 		    c.addItem(x);
 		}
 		else if (yAxis.matches())
 		{
 			// System.out.println("curve yAxis 1: " + curveYAxis.group(1));
 			// System.out.println("curve yAxis 2: " + curveYAxis.group(2));
 			// System.out.println("curve yAxis 3: " + curveYAxis.group(3));
 		}
 		else if (xBins.matches())
 		{
             uk.org.smithfamily.utils.normaliser.curveeditor.XBins x = new uk.org.smithfamily.utils.normaliser.curveeditor.XBins(xBins.group(1), xBins.group(3),xBins.group(5));
             c.addItem(x);
 		}
 		else if (yBins.matches())
 		{
             uk.org.smithfamily.utils.normaliser.curveeditor.YBins x = new uk.org.smithfamily.utils.normaliser.curveeditor.YBins(yBins.group(1));
             c.addItem(x);
 		}
 		else if (gauge.matches())
 		{
 		    LineLabel x = new LineLabel(gauge.group(1));
 		    c.addItem(x);
 		}
 		else if (lineLabel.matches())
         {
 	        LineLabel x = new LineLabel(lineLabel.group(1));
 	        c.addItem(x);
         }
 		else
         {
 		    uk.org.smithfamily.utils.normaliser.curveeditor.PreProcessor p = new uk.org.smithfamily.utils.normaliser.curveeditor.PreProcessor(processPreprocessor(ecuData, line));
             if (c != null && !c.isDefinitionCompleted())
             {
                 c.addItem(p);
             }
             else
             {
                 c = new CurveTracker();
                 curveDefs.add(c);
                 c.addItem(p);
             }
         }
 	}
 
 	/**
 	 * Process the [UserDefined] section of the ini file
 	 * 
 	 * @param line
 	 */
 	static void processUserDefined(ECUData ecuData, String line)
 	{
 		line = removeComments(line);
 		if (StringUtils.isEmpty(line))
 		{
 			return;
 		}
 
 		Matcher dialog = Patterns.dialog.matcher(line);
 		Matcher dialogField = Patterns.dialogField.matcher(line);
 		Matcher dialogDisplayOnlyField = Patterns.dialogDisplayOnlyField
 				.matcher(line);
 		Matcher dialogPanel = Patterns.dialogPanel.matcher(line);
 
 		if (dialog.matches())
 		{
 			// System.out.println("dialog Name: " + dialog.group(1));
 			// System.out.println("dialog Label: " + dialog.group(2));
 		}
 		else if (dialogField.matches())
 		{
 			// System.out.println("dialogField Label: " + dialogField.group(1));
 			// System.out.println("dialogField Name: " + dialogField.group(3));
 			// System.out.println("dialogField Expression: " +
 			// dialogField.group(5));
 		}
 		else if (dialogDisplayOnlyField.matches())
 		{
 			// System.out.println("dialogDisplayOnlyField Label: " +
 			// dialogDisplayOnlyField.group(1));
 			// System.out.println("dialogDisplayOnlyField Name: " +
 			// dialogDisplayOnlyField.group(3));
 			// System.out.println("dialogDisplayOnlyField Expression: " +
 			// dialogDisplayOnlyField.group(5));
 		}
 		else if (dialogPanel.matches())
 		{
 			// System.out.println("dialogPanel Name: " + dialogPanel.group(1));
 			// System.out.println("dialogPanel Orientation: " +
 			// dialogPanel.group(2));
 			// System.out.println("dialogPanel Expression: " +
 			// dialogPanel.group(4));
 		}
 	}
 
 	static void processConstantsExtensions(ECUData ecuData, String line)
 	{
 		line = removeComments(line);
 
 		if (line.contains("defaultValue"))
 		{
 			String statement = "";
 			String[] definition = line.split("=")[1].split(",");
 			if (definition[1].contains("\""))
 			{
 				statement = "String ";
 			}
 			else
 			{
 				statement = "int ";
 			}
 			statement += definition[0] + " = " + definition[1] + ";";
 			ecuData.getDefaults().add(statement);
 		}
 
 	}
 
 	static void processPcVariables(ECUData ecuData, String line)
 	{
 
 	}
 
 }

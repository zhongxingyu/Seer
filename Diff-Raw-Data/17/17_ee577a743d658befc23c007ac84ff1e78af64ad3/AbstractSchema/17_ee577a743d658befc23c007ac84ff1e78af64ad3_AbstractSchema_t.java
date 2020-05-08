 package eu.stratosphere.sopremo.serialization;
 
 import it.unimi.dsi.fastutil.ints.IntSet;
 
 import java.io.IOException;
 
 import eu.stratosphere.pact.common.type.PactRecord;
 import eu.stratosphere.pact.common.type.Value;
 import eu.stratosphere.sopremo.AbstractSopremoType;
 import eu.stratosphere.sopremo.pact.SopremoUtil;
 import eu.stratosphere.sopremo.type.IJsonNode;
 import eu.stratosphere.util.reflect.ReflectUtil;
 
 /**
  * Base class for all schema that build upon {@link JsonNodeWrapper}.
  * 
  * @author Arvid Heise
  */
 public abstract class AbstractSchema extends AbstractSopremoType implements Schema {
 	private final IntSet keyIndices;
 
	private final transient SopremoRecord inputRecord, outputRecord;
 
 	protected AbstractSchema(final int numFields, final IntSet keyIndices) {
 		if (keyIndices == null)
 			throw new NullPointerException();
 		this.keyIndices = keyIndices;
 		this.inputRecord = new SopremoRecord(numFields);
 		this.outputRecord = new SopremoRecord(numFields);
 	}
 
 	@Override
 	public IntSet getKeyIndices() {
 		return this.keyIndices;
 	}
 
 	@Override
 	public Class<? extends Value>[] getPactSchema() {
 		return this.inputRecord.getPactSchema();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.sopremo.serialization.Schema#jsonToRecord(eu.stratosphere.sopremo.type.IJsonNode,
 	 * eu.stratosphere.pact.common.type.PactRecord)
 	 */
 	@Override
 	public void jsonToRecord(IJsonNode value, PactRecord target) {
 		target.clear();
 		this.outputRecord.setRecord(target);
 		this.inputRecord.updateChangedFields();
 		this.jsonToRecord(value, this.outputRecord);
 	}
 
 	public abstract void jsonToRecord(IJsonNode value, SopremoRecord target);
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.sopremo.serialization.Schema#recordToJson(eu.stratosphere.pact.common.type.PactRecord)
 	 */
 	@Override
 	public IJsonNode recordToJson(PactRecord record) {
 		if (record.getNumFields() != this.inputRecord.getPactSchema().length)
 			throw new IllegalArgumentException();
 
 		this.inputRecord.setRecord(record);
 		final IJsonNode value = this.recordToJson(this.inputRecord);
 		return value;
 	}
 
 	public abstract IJsonNode recordToJson(SopremoRecord record);
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.sopremo.ISopremoType#appendAsString(java.lang.Appendable)
 	 */
 	@Override
 	public void appendAsString(Appendable appendable) throws IOException {
 		SopremoUtil.append(appendable, this.getClass().getSimpleName(), " [");
 		Class<? extends Value>[] pactSchema = this.inputRecord.getPactSchema();
 		for (int index = 0; index < pactSchema.length; index++)
 			appendable.append(pactSchema[index].getSimpleName()).append(' ');
 		appendable.append(']');
 	}
 
 	protected void kryoInit(final int numFields, final IntSet keyIndices) {
 		ReflectUtil.setField(this, AbstractSchema.class, "inputRecord", new SopremoRecord(numFields));
 		ReflectUtil.setField(this, AbstractSchema.class, "outputRecord", new SopremoRecord(numFields));
 		ReflectUtil.setField(this, AbstractSchema.class, "keyIndices", keyIndices);
 	}
 }

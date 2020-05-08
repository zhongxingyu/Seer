 package org.hbird.transport.payloadcodec;
 
 import java.util.BitSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang.StringUtils;
 import org.hbird.core.commons.data.GenericPayload;
 import org.hbird.core.commons.util.BitSetUtility;
 import org.hbird.core.commons.util.BytesUtility;
 import org.hbird.core.spacesystemmodel.encoding.Encoding;
 import org.hbird.core.spacesystemmodel.tmtc.CommandGroup;
 import org.hbird.core.spacesystemmodel.tmtc.Parameter;
 import org.hbird.core.spacesystemmodel.tmtc.ParameterGroup;
 import org.hbird.core.spacesystemmodel.tmtc.TmTcGroup;
 import org.hbird.core.spacesystemmodel.tmtc.provided.TmTcGroups;
 import org.hbird.transport.payloadcodec.codecdecorators.CommandGroupCodecDecorator;
 import org.hbird.transport.payloadcodec.codecdecorators.ParameterGroupCodecDecorator;
 import org.hbird.transport.payloadcodec.codecparameters.CodecParameter;
 import org.hbird.transport.payloadcodec.exceptions.NoEncodingException;
 import org.hbird.transport.payloadcodec.exceptions.UnexpectedParameterTypeException;
 import org.hbird.transport.payloadcodec.exceptions.UnknownParameterEncodingException;
 import org.hbird.transport.payloadcodec.exceptions.UnsupportedParameterEncodingException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class InMemoryPayloadCodec implements PayloadCodec {
 	private static final Logger LOG = LoggerFactory.getLogger(InMemoryPayloadCodec.class);
 
 	protected Map<String, ParameterGroup> parameterGroups = null;
 
 	private Map<String, ParameterGroup> codecAwareParameterGroups = null;
 
 	protected Map<String, CommandGroup> commandGroups = null;
 
 	private Map<String, CommandGroup> codecAwareCommandGroups = null;
 
 	protected final Map<String, Encoding> encodings;
 
 	protected final Map<String, List<String>> restrictions;
 
 	public InMemoryPayloadCodec(final Map<String, ParameterGroup> parameterGroups, final Map<String, CommandGroup> commandGroups,
 			final Map<String, Encoding> encodings, final Map<String, List<String>> restrictions) {
 		this.parameterGroups = parameterGroups;
 		this.commandGroups = commandGroups;
 		this.encodings = encodings;
 		this.restrictions = restrictions;
 
 		if (LOG.isTraceEnabled()) {
 			LOG.trace("Constructed with " + parameterGroups.size() + " parameterGroups, " + encodings.size() + " encodings, and " + restrictions.size()
 					+ " restrictions");
 		}
 
 		final ParameterGroupCodecDecorator parameterGroupDecorator = new ParameterGroupCodecDecorator(this.encodings);
 		final CommandGroupCodecDecorator commandGroupDecorator = new CommandGroupCodecDecorator(this.encodings);
 
 		try {
 			if (parameterGroups != null) {
 				this.codecAwareParameterGroups = parameterGroupDecorator.decorateParameterGroups(parameterGroups);
 			}
 			else {
 				LOG.debug("No parameter groups to decorate");
 			}
 			if (LOG.isTraceEnabled()) {
 				LOG.trace("Parameter groups have been cloned and decorated with codec aware parameters");
 			}
 
 			if (commandGroups != null) {
 				this.codecAwareCommandGroups = commandGroupDecorator.decorateParameterGroups(commandGroups);
 			}
 			else {
 				LOG.debug("No command groups to decorate");
 			}
 			if (LOG.isTraceEnabled()) {
 				LOG.trace("Command groups have been cloned and decorated with codec aware parameters");
 			}
 		}
 		catch (final NoEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (final UnsupportedParameterEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (final UnknownParameterEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (final UnexpectedParameterTypeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public ParameterGroup decode(final byte[] payload, final List<String> payloadLayoutIds, final long timeStamp) {
 		return decode(BitSetUtility.fromByteArray(payload), payloadLayoutIds, timeStamp);
 	}
 
 	@Override
 	public ParameterGroup decode(final BitSet payload, final List<String> payloadLayoutIds, final long timeStamp) {
 		ParameterGroup decodedGroup = null;
 		if (payloadLayoutIds == null || payloadLayoutIds.isEmpty()) {
 			// no restrictions, decode everything!
 			for (final ParameterGroup pg : codecAwareParameterGroups.values()) {
 				decodedGroup = decodeParameterGroup(payload, pg, timeStamp);
 			}
 		}
 		else {
 			boolean foundRestriction = false;
 			for (final Entry<String, List<String>> restrictionEntry : restrictions.entrySet()) {
 				int count = 0;
 				for (String restrictionValue : restrictionEntry.getValue()) {
 					if (!restrictionValue.equals(payloadLayoutIds.get(count++))) {
						foundRestriction = false;
 						break;
 					}
 					// we found the correct PG
 					foundRestriction = true;
 					final String pgName = restrictionEntry.getKey();
 					final ParameterGroup pg = codecAwareParameterGroups.get(pgName);
 					decodedGroup = decodeParameterGroup(payload, pg, timeStamp);
 				}
 			}
 			if (!foundRestriction) {
 				LOG.error("Payload codec did not find restriction for payloadLayoutId " + payloadLayoutIds + " in the restrictions entryset.");
 			}
 		}
 
 		return decodedGroup;
 	}
 
 	// TODO javadoc
 	private ParameterGroup decodeParameterGroup(final BitSet payload, final ParameterGroup pg, final long timestamp) {
 		int offset = 0;
 		int previousSize = 0;
 		int count = 0;
 		for (final Parameter<?> p : pg.getAllParameters().values()) {
 			if (count != 0) {
 				offset += previousSize;
 			}
 			final Encoding enc = this.encodings.get(p.getQualifiedName());
 			((CodecParameter<?>) p).decode(payload, offset);
 			p.setReceivedTime(timestamp);
 			previousSize = enc.getSizeInBits();
 			count++;
 		}
 
 		return getUndecoratedVersion(pg);
 	}
 
 	private ParameterGroup getUndecoratedVersion(final ParameterGroup pg) {
 		// get the name of the pg
 		final String name = pg.getName();
 		// find it in the undecorated version
 		ParameterGroup undecoratedGroup = null;
 		for (final ParameterGroup group : parameterGroups.values()) {
 			if (StringUtils.equals(group.getName(), name)) {
 				// set the value of the parameters in the undecorated version
 				undecoratedGroup = (ParameterGroup) TmTcGroups.copyAllParameterValues(pg, group);
 			}
 		}
 		// return the undecorated version
 		return undecoratedGroup;
 	}
 
 	@Override
 	public byte[] encodeToByteArray(final ParameterGroup parameterGroup) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("encodeToByteArray not yet implemented in InMemeoryPayloadCodec");
 	}
 
 	@Override
 	public GenericPayload encodeToGenericPayload(final TmTcGroup tmtcGroup) {
 		if (LOG.isTraceEnabled()) {
 			LOG.trace("Encoding TMTC Group " + tmtcGroup.getQualifiedName() + " to GenericPayload");
 		}
 
 		// FIXME Bit inefficient to search through both groups when commanding is majority of use-case.
 		final String undecoratedGroupName = tmtcGroup.getQualifiedName();
 		TmTcGroup decoratedCommandGroup = findInCodecCommandGroups(undecoratedGroupName);
 		TmTcGroup decoratedParameterGroup = findInCodecParameterGroups(undecoratedGroupName);
 
 		if (decoratedParameterGroup != null) {
 			InMemoryPayloadCodec.setValuesInCodecGroup(tmtcGroup, decoratedParameterGroup);
 			return encodeTmTcGroup(decoratedParameterGroup);
 		}
 
 		if (decoratedCommandGroup != null) {
 			InMemoryPayloadCodec.setValuesInCodecGroup(tmtcGroup, decoratedCommandGroup);
 			return encodeTmTcGroup(decoratedCommandGroup);
 		}
 
 		// FIXME Exception rather than null?
 		return null;
 	}
 
 	private final GenericPayload encodeTmTcGroup(final TmTcGroup tmTcGroup) {
 		// Carry out the encode
 		final BitSet encoded = new BitSet();
 		int count = 0;
 		int offset = 0;
 		int previousSize = 0;
 		int totalSize = 0;
 		for (final Parameter<?> p : tmTcGroup.getAllParameters().values()) {
 			if (LOG.isTraceEnabled()) {
 				LOG.trace("Encoding parameter " + p.getName());
 			}
 			if (count != 0) {
 				offset += previousSize;
 			}
 			final Encoding enc = this.encodings.get(p.getQualifiedName());
 			((CodecParameter<?>) p).encodeToBitSet(encoded, offset);
 			previousSize = enc.getSizeInBits();
 			totalSize += enc.getSizeInBits();
 			count++;
 		}
 
 		final byte[] encodedBytes = BitSetUtility.toByteArray(encoded, totalSize, false);
 		final List<String> layoutIdList = restrictions.get(tmTcGroup.getQualifiedName());
 		String layoutId = "";
 		if (layoutIdList != null) {
 			layoutId = layoutIdList.get(0);
 		}
 		if (encodedBytes == null) {
 			LOG.error("byte array is null!");
 		}
 
 		// GenericPayload encodedGroup = new GenericPayload(encodedBytes, layoutId); // FIXME this is crap, says Mark.
 		// FIXME 2011-11-12: added timeStamp. Can't remember why this is crap? Me neither :\
 		// We should probably remove this; nobody remembers why it was deemed crap. Perhaps it's good!
 
 		if (LOG.isTraceEnabled()) {
 			LOG.trace("Final encoded bytes = " + BytesUtility.decimalDump(encodedBytes));
 		}
 		final GenericPayload encodedGroup = new GenericPayload(encodedBytes, layoutId, System.currentTimeMillis());
 		return encodedGroup;
 	}
 
 	/**
 	 * TODO This fact that this can be declared static could indicate that is doesn't necessarily belong in this class.
 	 * Check it out.
 	 * 
 	 * @param tmtcGroup
 	 * @param decoratedTmTcGroup
 	 */
 	private static void setValuesInCodecGroup(final TmTcGroup tmtcGroup, final TmTcGroup decoratedTmTcGroup) {
 		// Integers
 		final Map<String, Parameter<Integer>> integerParameters = tmtcGroup.getIntegerParameters();
 		if (integerParameters != null) {
 			for (final Parameter<Integer> undecoratedParameter : integerParameters.values()) {
 				for (final Parameter<Integer> decoratedParameter : decoratedTmTcGroup.getIntegerParameters().values()) {
 					if (!decoratedParameter.isReadOnly()) {
 						if (StringUtils.equals(undecoratedParameter.getName(), decoratedParameter.getName())) {
 							decoratedParameter.setValue(undecoratedParameter.getValue());
 						}
 					}
 				}
 			}
 		}
 
 		// Longs
 		final Map<String, Parameter<Long>> longParameters = tmtcGroup.getLongParameters();
 		if (longParameters != null) {
 			for (final Parameter<Long> undecoratedParameter : longParameters.values()) {
 				for (final Parameter<Long> decoratedParameter : decoratedTmTcGroup.getLongParameters().values()) {
 					if (!decoratedParameter.isReadOnly()) {
 						if (StringUtils.equals(undecoratedParameter.getName(), decoratedParameter.getName())) {
 							decoratedParameter.setValue(undecoratedParameter.getValue());
 						}
 					}
 				}
 			}
 		}
 
 		// Strings
 		final Map<String, Parameter<String>> stringParameters = tmtcGroup.getStringParameters();
 		if (stringParameters != null) {
 			for (final Parameter<String> undecoratedParameter : stringParameters.values()) {
 				for (final Parameter<String> decoratedParameter : decoratedTmTcGroup.getStringParameters().values()) {
 					if (!decoratedParameter.isReadOnly()) {
 						if (StringUtils.equals(undecoratedParameter.getName(), decoratedParameter.getName())) {
 							decoratedParameter.setValue(undecoratedParameter.getValue());
 						}
 					}
 				}
 			}
 		}
 
 		// FIXME Support for other types
 	}
 
 	private ParameterGroup findInCodecParameterGroups(final String undecoratedGroupName) {
 		if (codecAwareParameterGroups != null) {
 			return codecAwareParameterGroups.get(undecoratedGroupName);
 		}
 		return null;
 	}
 
 	private CommandGroup findInCodecCommandGroups(final String undecoratedGroupName) {
 		if (codecAwareCommandGroups != null) {
 			return codecAwareCommandGroups.get(undecoratedGroupName);
 		}
 		return null;
 	}
 
 	@Override
 	public ParameterGroup decode(final GenericPayload payload) {
 		if (LOG.isTraceEnabled()) {
 			LOG.trace("Decoding: " + BytesUtility.decimalDump(payload.payload) + " with payload ID " + payload.layoutIdentifiers);
 		}
 		return decode(payload.payload, payload.layoutIdentifiers, payload.timeStamp);
 	}
 
 }

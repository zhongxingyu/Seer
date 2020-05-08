 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.services.repositoryservice.impl;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
 import org.ebayopensource.turmeric.common.v1.types.ErrorParameter;
 import org.ebayopensource.turmeric.errorlibrary.repository.ErrorConstants;
 
 import org.ebayopensource.turmeric.repository.v2.services.*;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorDataFactory;
 import org.ebayopensource.turmeric.services.repositoryservice.operation.util.RepositoryServiceOperationValidationUtil;
 
 public class RepositoryServiceValidateUtil {
 
 	public static boolean validate(ApproveAssetRequest approveAssetRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 
 		if (approveAssetRequest.getApprovalInfo() != null) {
 
 			ApprovalInfo approvalInfo = approveAssetRequest.getApprovalInfo();
 			if (approvalInfo.getApproverRole() == null) {
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.APPROVAL_ROLE_MISSING,
 						ErrorConstants.ERRORDOMAIN));
 			}
 			if (approvalInfo.getAssetId() == null
 					&& approvalInfo.getAssetDetail() != null) {
 
 				if (approvalInfo.getAssetDetail().getAssetName() == null) {
 					errorDataList.add(ErrorDataFactory.createErrorData(
 							ErrorConstants.ASSET_NAME_AND_ID_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 				} else if (approvalInfo.getAssetDetail().getAssetType() == null) {
 					errorDataList.add(ErrorDataFactory.createErrorData(
 							ErrorConstants.ASSET_TYPE_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 				}
 			}
 
 		} else {
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.APPROVAL_INFO_MISSING,
 					ErrorConstants.ERRORDOMAIN));
 		}
 
 		if (errorDataList.size() > 0)
 			isValid = false;
 		return isValid;
 	}
 
 	public static boolean validate(
 			CreateAndSubmitAssetRequest createAndSubmitAssetRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 
 		if (createAndSubmitAssetRequest != null) {
 			CreateCompleteAssetRequest createCompleteAssetRequest = getCreateCompleteAssetRequest(createAndSubmitAssetRequest);
 
 			isValid = validate(createCompleteAssetRequest, errorDataList);
 		} else {
 			isValid = false;
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.REQUEST_EMPTY, ErrorConstants.ERRORDOMAIN));
 		}
 
 		return isValid;
 	}
 
 	private static CreateCompleteAssetRequest getCreateCompleteAssetRequest(
 			CreateAndSubmitAssetRequest createAndSubmitAssetRequest) {
 
 		CreateCompleteAssetRequest createCompleteAssetRequest = new CreateCompleteAssetRequest();
 		createCompleteAssetRequest.setAssetInfo(createAndSubmitAssetRequest
 				.getAssetInfo());
		createCompleteAssetRequest
				.setCaptureTemplateName(createAndSubmitAssetRequest
						.getCaptureTemplateName());
 		return createCompleteAssetRequest;
 	}
 
 	public static boolean validate(CreateAssetRequest createAssetRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 
 		if (createAssetRequest != null) {
 			if (createAssetRequest.getBasicAssetInfo() != null) {
 				if (createAssetRequest.getBasicAssetInfo().getAssetName() == null
 						&&
 
 						(createAssetRequest.getBasicAssetInfo().getAssetKey() == null ||
 
 						createAssetRequest.getBasicAssetInfo().getAssetKey().getAssetName()	 == null)) {
 					isValid = false;
 					errorDataList.add(ErrorDataFactory.createErrorData(
 							ErrorConstants.ASSET_NAME_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 				} else {
 					if (createAssetRequest.getBasicAssetInfo().getAssetName() == null) {
 
 						createAssetRequest.getBasicAssetInfo().setAssetName(
 								createAssetRequest.getBasicAssetInfo()
 										.getAssetKey().getAssetName());
 					} else {
 						if (createAssetRequest.getBasicAssetInfo()
 								.getAssetKey() == null) {
 							createAssetRequest.getBasicAssetInfo().setAssetKey(
 									new
 
 									AssetKey());
 						}
 
 						createAssetRequest
 								.getBasicAssetInfo()
 								.getAssetKey()
 								.setAssetName(
 										createAssetRequest.getBasicAssetInfo()
 												.getAssetName());
 					}
 				}
 				if (createAssetRequest.getBasicAssetInfo().getVersion() == null) {
 					isValid = false;
 					errorDataList.add(ErrorDataFactory.createErrorData(
 							ErrorConstants.ASSET_VERSION_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 				}
 
 				if (createAssetRequest.getBasicAssetInfo().getGroupName() == null) {
 					isValid = false;
 					String assetName = createAssetRequest.getBasicAssetInfo()
 							.getAssetName();
 					CommonErrorData errorData = ErrorDataFactory
 							.createErrorData(ErrorConstants.GROUP_NOT_PROVIDED,
 									ErrorConstants.ERRORDOMAIN,
 									new String[] { assetName });
 					errorDataList.add(errorData);
 				}
 
 				if (createAssetRequest.getBasicAssetInfo().getAssetType() == null) {
 					isValid = false;
 					errorDataList.add(ErrorDataFactory.createErrorData(
 							ErrorConstants.ASSET_TYPE_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 				}
 				if (createAssetRequest.getBasicAssetInfo().getVersion() != null) {
 
 					if (!checkVersionFormat(createAssetRequest
 							.getBasicAssetInfo().getVersion(), 3)) {
 						isValid = false;
 						String version = createAssetRequest.getBasicAssetInfo()
 								.getVersion();
 						CommonErrorData errorData = ErrorDataFactory
 								.createErrorData(
 										ErrorConstants.INVALID_VERSION,
 										ErrorConstants.ERRORDOMAIN,
 										new String[] { version });
 						errorDataList.add(errorData);
 					}
 				}
 				if (createAssetRequest.getBasicAssetInfo().getAssetKey() == null) {
 					isValid = false;
 					errorDataList.add(ErrorDataFactory.createErrorData(
 							ErrorConstants.LIBRARY_ID_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 				} else {
 					if (createAssetRequest.getBasicAssetInfo().getAssetKey()
 							.getAssetId() != null) {
 						errorDataList.add(ErrorDataFactory.createErrorData(
 								ErrorConstants.ASSET_ID_NOT_NEEDED,
 								ErrorConstants.ERRORDOMAIN));
 					}
 				}
 			} else {
 				isValid = false;
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.LIBRARY_ID_MISSING,
 						ErrorConstants.ERRORDOMAIN));
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.ASSET_NAME_MISSING,
 						ErrorConstants.ERRORDOMAIN));
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.ASSET_TYPE_MISSING,
 						ErrorConstants.ERRORDOMAIN));
 			}
 		} else {
 			isValid = false;
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.REQUEST_EMPTY, ErrorConstants.ERRORDOMAIN));
 		}
 
 		if (errorDataList.size() > 0)
 			isValid = false;
 		return isValid;
 
 	}
 
 	public static boolean validate(
 			CreateCompleteAssetRequest createCompleteAssetRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 
 		if (createCompleteAssetRequest != null) {
 			isValid = RepositoryServiceOperationValidationUtil
 					.validateAssetInfo(
 							createCompleteAssetRequest.getAssetInfo(),
 							errorDataList);

			if (createCompleteAssetRequest.getCaptureTemplateName() == null) {
				errorDataList.add(ErrorDataFactory.createErrorData(
						ErrorConstants.CAPTURE_TEMPLATE_NOT_RESOLVED,
						ErrorConstants.ERRORDOMAIN));
				isValid = false;
			}
 		} else {
 			errorDataList.add(ErrorDataFactory
 					.createErrorData(ErrorConstants.NO_REQUEST_PARAM,
 							ErrorConstants.ERRORDOMAIN));
 			isValid = false;
 		}
 
 		return isValid;
 	}
 
 	/**
 	 * This method validates a version format. It assumes that each part of the
 	 * version would be a numeric value.
 	 * 
 	 * @param input
 	 *            The input String to be verified for version pattern
 	 *            compliance.
 	 * @param level
 	 *            The number of major and minor versions etc .. for somethng
 	 *            like 1.0 the level is two , and for something like 1.0.4 the
 	 *            level is 3.
 	 * @return
 	 */
 	public static boolean checkVersionFormat(String input, int level) {
 		String patternStr = "";
 		StringBuffer strBuf = new StringBuffer();
 		String onePatternStr = "[0-9]+.";
 		for (int i = 0; i < level; i++) {
 			strBuf.append(onePatternStr);
 		}
 
 		patternStr = strBuf.toString();
 		patternStr = patternStr.substring(0, patternStr.length() - 1); // remove
 																		// the
 																		// extra
 																		// dot
 																		// at
 																		// the
 																		// end
 
 		Pattern regexPattern = Pattern.compile(patternStr);
 		Matcher regexMatcher = regexPattern.matcher(input);
 		if (regexMatcher.matches())
 			return true;
 		else
 			return false;
 	}
 
 	public static boolean validate(
 			GetAllAssetsGroupedByCategoryRequest getAllAssetsGroupedByCategoryRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 
 		if (getAllAssetsGroupedByCategoryRequest == null) {
 			isValid = false;
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.REQUEST_EMPTY, ErrorConstants.ERRORDOMAIN));
 		}
 		return isValid;
 	}
 
 
 	public static boolean validate(
 			GetAssetDependenciesRequest getAssetDependenciesRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 
 		if (getAssetDependenciesRequest.getAssetKey() != null) {
 			if (getAssetDependenciesRequest.getAssetKey().getAssetId() == null) {
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.ASSET_ID_MISSING,
 						ErrorConstants.ERRORDOMAIN));
 			}
 		} else {
 			errorDataList.add(ErrorDataFactory
 					.createErrorData(ErrorConstants.ASSET_ID_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 		}
 
 		if (errorDataList.size() > 0)
 			isValid = false;
 		return isValid;
 	}
 
 	/**
 	 * Validates the input
 	 * 
 	 * @param getAssetInfoRequest
 	 * @param errorDataList
 	 * @return
 	 */
 	public static boolean validate(GetAssetInfoRequest getAssetInfoRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 		if (getAssetInfoRequest.getAssetKey() != null) {
 
 			if (getAssetInfoRequest.getAssetKey().getAssetId() == null) {
 				if (getAssetInfoRequest.getAssetKey().getAssetName() == null) {
 					errorDataList.add(ErrorDataFactory.createErrorData(
 							ErrorConstants.ASSET_NAME_AND_ID_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 				} else {
 					if (getAssetInfoRequest.getAssetType() == null)
 						errorDataList.add(ErrorDataFactory.createErrorData(
 								ErrorConstants.ASSET_TYPE_MISSING,
 								ErrorConstants.ERRORDOMAIN));
 				}
 			}
 		} else {
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.ASSET_NAME_AND_ID_MISSING,
 					ErrorConstants.ERRORDOMAIN));
 		}
 
 		if (errorDataList.size() > 0)
 			isValid = false;
 		return isValid;
 	}
 
 	/**
 	 * Validates the input
 	 * 
 	 * @param updateServiceRequest
 	 * @param errorDataList
 	 * @return
 	 */
 	public static boolean validate(
 			GetAssetLifeCycleStatesRequest getAssetLifeCycleStatesRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 		if (getAssetLifeCycleStatesRequest.getAssetType() == null) {
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.ASSET_TYPE_MISSING,
 					ErrorConstants.ERRORDOMAIN));
 		}
 
 		if (errorDataList.size() > 0)
 			isValid = false;
 		return isValid;
 	}
 
 	/**
 	 * Used to validate the input for GetAssetStatus operation
 	 * 
 	 * @param getAssetStatusRequest
 	 * @param errorDataList
 	 * @return
 	 */
 	public static boolean validate(GetAssetStatusRequest getAssetStatusRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 
 		if (getAssetStatusRequest.getAssetKey() != null) {
 			if (getAssetStatusRequest.getAssetKey().getAssetId() == null) {
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.ASSET_ID_MISSING,
 						ErrorConstants.ERRORDOMAIN));
 			}
 		} else {
 			errorDataList.add(ErrorDataFactory
 					.createErrorData(ErrorConstants.ASSET_ID_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 		}
 		if (errorDataList.size() > 0)
 			isValid = false;
 		return isValid;
 	}
 
 	/**
 	 * This validates the request object for the operation to see whether any
 	 * required parameters are missing.
 	 * 
 	 * @param getAssetSubmissionPropertiesRequest
 	 *            request to be validated
 	 * @param errorDataList
 	 *            this will hold any errors during validation
 	 * @return boolean indicating success or failure or request object
 	 *         validation
 	 */
 	public static boolean validate(
 			GetAssetSubmissionPropertiesRequest getAssetSubmissionPropertiesRequest,
 			List<CommonErrorData> errorDataList) {
 
 		boolean isValid = true;
 		if (getAssetSubmissionPropertiesRequest == null) {
 			isValid = false;
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.REQUEST_EMPTY, ErrorConstants.ERRORDOMAIN));
 
 		} else {
 			if (getAssetSubmissionPropertiesRequest.getAssetKey() != null) {
 				if (getAssetSubmissionPropertiesRequest.getAssetKey()
 						.getAssetId() == null) {
 					isValid = false;
 					errorDataList.add(ErrorDataFactory.createErrorData(
 							ErrorConstants.ASSET_ID_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 				}
 			} else {
 				isValid = false;
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.ASSET_ID_MISSING,
 						ErrorConstants.ERRORDOMAIN));
 			}
 		}
 		return isValid;
 	}
 
 	public static boolean validate(
 			GetAssetTreeByAttributesRequest getAssetTreeByAttributesRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 
 		if (getAssetTreeByAttributesRequest != null) {
 			if (getAssetTreeByAttributesRequest.getNextLevelAttribute() == null
 					|| getAssetTreeByAttributesRequest.getNextLevelAttribute()
 							.equals("")) {
 				isValid = false;
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.NEXT_LEVEL_ATTRIBUTE_MISSING,
 						ErrorConstants.ERRORDOMAIN));
 			}
 		} else {
 			isValid = false;
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.REQUEST_EMPTY, ErrorConstants.ERRORDOMAIN));
 		}
 
 		return isValid;
 	}
 
 	public static boolean validate(GetAssetTypesRequest getAssetTypesRequest,
 			List<CommonErrorData> errorDataList) {
 		// validations if any in the future
 		return true;
 	}
 
 	/**
 	 * Used to validate the input for GetAssetVersions operation
 	 * 
 	 * @param getAssetVersionsRequest
 	 * @param errorDataList
 	 * @return
 	 */
 	public static boolean validate(
 			GetAssetVersionsRequest getAssetVersionsRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 
 		if (getAssetVersionsRequest.getAssetKey() != null) {
 			if (getAssetVersionsRequest.getAssetKey().getAssetId() == null
 					&& getAssetVersionsRequest.getAssetKey().getAssetName() == null) {
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.ASSET_NAME_AND_ID_MISSING,
 						ErrorConstants.ERRORDOMAIN));
 			}
 		} else {
 			errorDataList.add(ErrorDataFactory
 					.createErrorData(ErrorConstants.ASSET_ID_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.LIBRARY_NAME_MISSING,
 					ErrorConstants.ERRORDOMAIN));
 		}
 		if (errorDataList.size() > 0)
 			isValid = false;
 		return isValid;
 	}
 
 	/**
 	 * Used to validate the input for GetBasicAssetInfo operation
 	 * 
 	 * @param getBasicAssetInfoRequest
 	 * @param errorDataList
 	 * @return
 	 */
 	public static boolean validate(
 			GetBasicAssetInfoRequest getBasicAssetInfoRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 
 		if (getBasicAssetInfoRequest.getAssetKey() != null) {
 			if (getBasicAssetInfoRequest.getAssetKey().getAssetId() == null) {
 				if (getBasicAssetInfoRequest.getAssetKey().getAssetName() == null) {
 					errorDataList.add(ErrorDataFactory.createErrorData(
 							ErrorConstants.ASSET_NAME_AND_ID_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 				} else {
 					if (getBasicAssetInfoRequest.getAssetType() == null) {
 						errorDataList.add(ErrorDataFactory.createErrorData(
 								ErrorConstants.ASSET_TYPE_MISSING,
 								ErrorConstants.ERRORDOMAIN));
 					}
 				}
 			}
 		} else {
 			errorDataList.add(ErrorDataFactory
 					.createErrorData(ErrorConstants.ASSET_ID_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.LIBRARY_NAME_MISSING,
 					ErrorConstants.ERRORDOMAIN));
 		}
 		if (errorDataList.size() > 0)
 			isValid = false;
 		return isValid;
 
 	}
 
 	public static boolean validate(LockAssetRequest lockAssetRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 		if (lockAssetRequest.getAssetKey() != null) {
 			if (lockAssetRequest.getAssetKey().getAssetId() == null) {
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.ASSET_ID_MISSING,
 						ErrorConstants.ERRORDOMAIN));
 			}
 		} else {
 			errorDataList.add(ErrorDataFactory
 					.createErrorData(ErrorConstants.ASSET_ID_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.LIBRARY_NAME_MISSING,
 					ErrorConstants.ERRORDOMAIN));
 		}
 		if (errorDataList.size() > 0)
 			isValid = false;
 		return isValid;
 	}
 
 	/**
 	 * Validates the input
 	 * 
 	 * @param rejectAssetRequest
 	 * @param errorDataList
 	 * @return
 	 */
 	public static boolean validate(RejectAssetRequest rejectAssetRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 
 		if (rejectAssetRequest.getRejectionInfo() != null) {
 
 			RejectionInfo rejectionInfo = rejectAssetRequest.getRejectionInfo();
 			if (rejectionInfo.getRejectionRole() == null) {
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.REJECTION_ROLE_MISSING,
 						ErrorConstants.ERRORDOMAIN));
 			}
 			if (rejectionInfo.getAssetId() == null
 					&& rejectionInfo.getAssetDetail() != null) {
 
 				if (rejectionInfo.getAssetDetail().getAssetName() == null) {
 					errorDataList.add(ErrorDataFactory.createErrorData(
 							ErrorConstants.ASSET_NAME_AND_ID_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 				} else if (rejectionInfo.getAssetDetail().getAssetType() == null) {
 					errorDataList.add(ErrorDataFactory.createErrorData(
 							ErrorConstants.ASSET_TYPE_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 				}
 			}
 
 		} else {
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.REJECTION_INFO_MISSING,
 					ErrorConstants.ERRORDOMAIN));
 		}
 
 		if (errorDataList.size() > 0)
 			isValid = false;
 		return isValid;
 	}
 
 	public static boolean validate(RemoveAssetRequest removeAssetRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 		if (removeAssetRequest != null) {
 			if (removeAssetRequest.getAssetKey() != null) {
 				if (removeAssetRequest.getAssetKey().getAssetId() == null) {
 					isValid = false;
 					errorDataList.add(ErrorDataFactory.createErrorData(
 							ErrorConstants.ASSET_ID_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 				}
 			} else {
 				isValid = false;
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.ASSET_ID_MISSING,
 						ErrorConstants.ERRORDOMAIN));
 			}
 		} else {
 			isValid = false;
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.REQUEST_EMPTY, ErrorConstants.ERRORDOMAIN));
 		}
 		return isValid;
 	}
 
 	public static boolean validate(SearchAssetsRequest searchAssetsRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 		if (searchAssetsRequest == null) {
 			isValid = false;
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.REQUEST_EMPTY, ErrorConstants.ERRORDOMAIN));
 		}
 		return isValid;
 	}
 
 	public static boolean validate(
 			SearchAssetsDetailedRequest searchAssetsDetailedRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 		if (searchAssetsDetailedRequest == null) {
 			isValid = false;
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.REQUEST_EMPTY, ErrorConstants.ERRORDOMAIN));
 		}
 		return isValid;
 	}
 
 	public static boolean validate(
 			SubmitForPublishingRequest submitForPublishingRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 
 		if (submitForPublishingRequest.getAssetKey() != null) {
 			if (submitForPublishingRequest.getAssetKey().getAssetId() == null) {
 				isValid = false;
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.ASSET_ID_MISSING,
 						ErrorConstants.ERRORDOMAIN));
 			}
 		} else {
 			isValid = false;
 			errorDataList.add(ErrorDataFactory
 					.createErrorData(ErrorConstants.ASSET_ID_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 		}
 		if (submitForPublishingRequest.getComment() != null
 				&& submitForPublishingRequest.getComment().length() > 1000) {
 			String shortenedComment = submitForPublishingRequest.getComment()
 					.substring(0, 999);
 			submitForPublishingRequest.setComment(shortenedComment);
 			String comment = submitForPublishingRequest.getComment();
 			CommonErrorData errorData = ErrorDataFactory.createErrorData(
 					ErrorConstants.COMMENT_TOO_LONG,
 					ErrorConstants.ERRORDOMAIN, new String[] { comment });
 			errorDataList.add(errorData);
 		}
 
 		return isValid;
 	}
 
 	public static boolean validate(UnlockAssetRequest unlockAssetRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 		if (unlockAssetRequest.getAssetKey() != null) {
 			if (unlockAssetRequest.getAssetKey().getAssetId() == null) {
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.ASSET_ID_MISSING,
 						ErrorConstants.ERRORDOMAIN));
 			}
 		} else {
 			errorDataList.add(ErrorDataFactory
 					.createErrorData(ErrorConstants.ASSET_ID_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 		}
 		if (errorDataList.size() > 0)
 			isValid = false;
 		return isValid;
 	}
 
 	public static boolean validate(
 			UpdateAssetArtifactsRequest updateAssetArtifactsRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 		if (updateAssetArtifactsRequest != null) {
 			if (updateAssetArtifactsRequest.getAssetKey() != null) {
 				if (updateAssetArtifactsRequest.getAssetKey().getAssetId() == null) {
 					errorDataList.add(ErrorDataFactory.createErrorData(
 							ErrorConstants.ASSET_ID_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 				}
 			} else {
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.ASSET_ID_MISSING,
 						ErrorConstants.ERRORDOMAIN));
 			}
 			if (updateAssetArtifactsRequest.getArtifactInfo() != null) {
 				if (updateAssetArtifactsRequest.getArtifactInfo().size() <= 0) {
 					errorDataList.add(ErrorDataFactory.createErrorData(
 							ErrorConstants.NO_ARTIFACTS_FOR_ADDING,
 							ErrorConstants.ERRORDOMAIN));
 				} else {
 					for (ArtifactInfo artifactInfo : updateAssetArtifactsRequest
 							.getArtifactInfo()) {
 						if (artifactInfo.getArtifact() != null) {
 							if (artifactInfo.getArtifact().getArtifactName() == null) {
 								errorDataList
 										.add(ErrorDataFactory
 												.createErrorData(
 														ErrorConstants.ARTIFACT_NAME_MISSING,
 														ErrorConstants.ERRORDOMAIN));
 							}
 							if (artifactInfo.getArtifact()
 									.getArtifactValueType() == null) {
 								errorDataList
 										.add(ErrorDataFactory
 												.createErrorData(
 														ErrorConstants.ARTIFACT_VALUE_TYPE_MISSING,
 														ErrorConstants.ERRORDOMAIN));
 							}
 							if (artifactInfo.getArtifact()
 									.getArtifactCategory() == null) {
 								errorDataList
 										.add(ErrorDataFactory
 												.createErrorData(
 														ErrorConstants.ARTIFACT_CATEGORY_MISSING,
 														ErrorConstants.ERRORDOMAIN));
 							}
 						} else {
 							errorDataList.add(ErrorDataFactory.createErrorData(
 									ErrorConstants.ARTIFACT_NAME_MISSING,
 									ErrorConstants.ERRORDOMAIN));
 							errorDataList.add(ErrorDataFactory.createErrorData(
 									ErrorConstants.ARTIFACT_VALUE_TYPE_MISSING,
 									ErrorConstants.ERRORDOMAIN));
 							errorDataList.add(ErrorDataFactory.createErrorData(
 									ErrorConstants.ARTIFACT_CATEGORY_MISSING,
 									ErrorConstants.ERRORDOMAIN));
 						}
 						if (artifactInfo.getArtifactDetail() == null) {
 							errorDataList.add(ErrorDataFactory.createErrorData(
 									ErrorConstants.ARTIFACT_EMPTY_EXCEPTION,
 									ErrorConstants.ERRORDOMAIN));
 						}
 					}
 				}
 			} else {
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.NO_ARTIFACTS_FOR_ADDING,
 						ErrorConstants.ERRORDOMAIN));
 			}
 		} else {
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.REQUEST_EMPTY, ErrorConstants.ERRORDOMAIN));
 		}
 		if (errorDataList.size() > 0)
 			isValid = false;
 		return isValid;
 	}
 
 	/**
 	 * Validates the input
 	 * 
 	 * @param updateAssetAttributesRequest
 	 * @param errorDataList
 	 * @return
 	 */
 	public static boolean validate(
 			UpdateAssetAttributesRequest updateAssetAttributesRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 		AssetKey assetKey = updateAssetAttributesRequest.getAssetKey();
 		if (assetKey != null) {
 			if (assetKey.getAssetId() == null)
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.ASSET_ID_MISSING,
 						ErrorConstants.ERRORDOMAIN));
 		} else {
 			errorDataList.add(ErrorDataFactory
 					.createErrorData(ErrorConstants.ASSET_ID_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.LIBRARY_NAME_MISSING,
 					ErrorConstants.ERRORDOMAIN));
 		}
 
 		if (errorDataList.size() > 0)
 			isValid = false;
 		return isValid;
 	}
 
 	public static boolean validate(
 			UpdateAssetDependenciesRequest updateAssetDependenciesRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 
 		if (updateAssetDependenciesRequest.getAssetKey() == null) {
 			isValid = false;
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.SOURCE_ASSET_NOT_ENTERED,
 					ErrorConstants.ERRORDOMAIN));
 		}
 
 		if (updateAssetDependenciesRequest.getFlattenedRelationshipForUpdate() != null) {
 			if (updateAssetDependenciesRequest
 					.getFlattenedRelationshipForUpdate().getSourceAsset() != null
 					&& updateAssetDependenciesRequest.getAssetKey() != null) {
 				if (updateAssetDependenciesRequest
 						.getFlattenedRelationshipForUpdate().getSourceAsset()
 						.getAssetId() == null
 						|| !updateAssetDependenciesRequest
 								.getFlattenedRelationshipForUpdate()
 								.getSourceAsset()
 								.getAssetId()
 								.equals(updateAssetDependenciesRequest
 										.getAssetKey().getAssetId())) {
 					isValid = false;
 					List<ErrorParameter> errorParameterList = new ArrayList<ErrorParameter>();
 
 					ErrorParameter firstAssetKeyErrorParameter = new ErrorParameter();
 					firstAssetKeyErrorParameter.setName("firstAssetKey");
 					firstAssetKeyErrorParameter
 							.setValue("source asset in the flattenedrelationship");
 					errorParameterList.add(firstAssetKeyErrorParameter);
 
 					ErrorParameter secondAssetKeyErrorParameter = new ErrorParameter();
 					secondAssetKeyErrorParameter.setName("secondAssetKey");
 					secondAssetKeyErrorParameter
 							.setValue("the updateassetRelationshipRequest");
 					errorParameterList.add(secondAssetKeyErrorParameter);
 					errorDataList.add(ErrorDataFactory.createErrorData(
 							ErrorConstants.ASSET_KEY_MISMATCH_ERROR,
 							ErrorConstants.ERRORDOMAIN));
 				}
 			} else {
 				updateAssetDependenciesRequest
 						.getFlattenedRelationshipForUpdate().setSourceAsset(
 								updateAssetDependenciesRequest.getAssetKey());
 			}
 			if (updateAssetDependenciesRequest
 					.getFlattenedRelationshipForUpdate().getRelatedAsset() == null) {
 				isValid = false;
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.RELATIONS_NOT_ENTERED,
 						ErrorConstants.ERRORDOMAIN));
 			} else {
 				List<RelationForUpdate> toUpdate = updateAssetDependenciesRequest
 						.getFlattenedRelationshipForUpdate().getRelatedAsset();
 				Iterator<RelationForUpdate> toUpdateItor = toUpdate.iterator();
 				while (toUpdateItor.hasNext()) {
 					RelationForUpdate relationForUpdate = (RelationForUpdate) toUpdateItor
 							.next();
 					if (!validateRelationForupdate(relationForUpdate,
 							errorDataList)) {
 						isValid = false;
 
 					}
 
 				}
 			}
 		} else {
 			isValid = false;
 		}
 		return isValid;
 	}
 
 	private static boolean validateRelationForupdate(
 			RelationForUpdate relationForUpdate,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 		if (relationForUpdate.getNewRelation() == null
 				&& !relationForUpdate.isDeleteRelation()) {
 			isValid = false;
 			List<ErrorParameter> errorParameterList = new ArrayList<ErrorParameter>();
 
 			ErrorParameter firstAssetKeyErrorParameter = new ErrorParameter();
 			firstAssetKeyErrorParameter.setName("firstAssetName");
 			if (relationForUpdate.getCurrentSourceAsset() != null) {
 				firstAssetKeyErrorParameter.setValue(relationForUpdate
 						.getCurrentSourceAsset().getAssetName());
 			}
 			errorParameterList.add(firstAssetKeyErrorParameter);
 
 			ErrorParameter secondAssetKeyErrorParameter = new ErrorParameter();
 			secondAssetKeyErrorParameter.setName("secondAssetName");
 			if (relationForUpdate.getCurrentTargetAsset() != null) {
 				firstAssetKeyErrorParameter.setValue(relationForUpdate
 						.getCurrentTargetAsset().getAssetName());
 			}
 			errorParameterList.add(secondAssetKeyErrorParameter);
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.NEW_RELATION_MISSING,
 					ErrorConstants.ERRORDOMAIN));
 		}
 		return isValid;
 	}
 
 	/**
 	 * Validates the input
 	 * 
 	 * @param updateAssetRequest
 	 * @param errorDataList
 	 * @return
 	 */
 	public static boolean validate(UpdateAssetRequest updateAssetRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 
 		if (updateAssetRequest != null) {
 			BasicAssetInfo basicAssetInfo = updateAssetRequest
 					.getBasicAssetInfo();
 
 			if (basicAssetInfo != null) {
 				if (basicAssetInfo.getVersion() != null) {
 					if (!checkVersionFormat(basicAssetInfo.getVersion(), 3)) {
 						String version = basicAssetInfo.getVersion();
 						CommonErrorData errorData = ErrorDataFactory
 								.createErrorData(
 										ErrorConstants.INVALID_VERSION,
 										ErrorConstants.ERRORDOMAIN,
 										new String[] { version });
 						errorDataList.add(errorData);
 					}
 				}
 				AssetKey assetKey = basicAssetInfo.getAssetKey();
 				if (assetKey != null) {
 					if (assetKey.getAssetId() == null)
 						errorDataList.add(ErrorDataFactory.createErrorData(
 								ErrorConstants.ASSET_ID_MISSING,
 								ErrorConstants.ERRORDOMAIN));
 				} else {
 					errorDataList.add(ErrorDataFactory.createErrorData(
 							ErrorConstants.ASSET_ID_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 					errorDataList.add(ErrorDataFactory.createErrorData(
 							ErrorConstants.LIBRARY_NAME_MISSING,
 							ErrorConstants.ERRORDOMAIN));
 				}
 			} else {
 				isValid = false;
 				errorDataList.add(ErrorDataFactory.createErrorData(
 						ErrorConstants.BASE_ASSETINFO_MISSING,
 						ErrorConstants.ERRORDOMAIN));
 			}
 		} else {
 			isValid = false;
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.REQUEST_EMPTY, ErrorConstants.ERRORDOMAIN));
 		}
 		if (errorDataList.size() > 0)
 			isValid = false;
 		return isValid;
 	}
 
 	public static boolean validate(
 			UpdateCompleteAssetRequest updateCompleteAssetRequest,
 			List<CommonErrorData> errorDataList) {
 		boolean isValid = true;
 
 		if (updateCompleteAssetRequest != null) {
 			isValid = RepositoryServiceOperationValidationUtil
 					.validateAssetInfoForUpdate(
 							updateCompleteAssetRequest.getAssetInfoForUpdate(),
 							errorDataList);
 		} else {
 			errorDataList.add(ErrorDataFactory
 					.createErrorData(ErrorConstants.NO_REQUEST_PARAM,
 							ErrorConstants.ERRORDOMAIN));
 			isValid = false;
 		}
 
 		return isValid;
 	}
 
 	public static boolean validate(ValidateAssetRequest validateAssetRequest,
 			List<CommonErrorData> errorDataList) {
 
 		boolean isValid = true;
 
 		if (validateAssetRequest == null) {
 			isValid = false;
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.REQUEST_EMPTY, ErrorConstants.ERRORDOMAIN));
 		} else if (validateAssetRequest.getAssetInfo() == null) {
 			isValid = false;
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.MISSING_ASSETINFO,
 					ErrorConstants.ERRORDOMAIN));
 		} else if (validateAssetRequest.getAssetInfo().getBasicAssetInfo() == null
 				|| validateAssetRequest.getAssetInfo().getBasicAssetInfo()
 						.getAssetKey() == null) {
 			isValid = false;
 			errorDataList.add(ErrorDataFactory.createErrorData(
 					ErrorConstants.ASSET_NAME_AND_ID_MISSING,
 					ErrorConstants.ERRORDOMAIN));
 		}
 		return isValid;
 	}
 
 }
